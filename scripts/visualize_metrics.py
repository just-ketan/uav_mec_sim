#!/usr/bin/env python3

from pathlib import Path
import argparse
import sys
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

sns.set_theme(style="whitegrid")  # makes plots look nicer; optional

EXPECTED_COLUMNS = ["cost", "latency", "eventtype", "successful"]

def find_columns(df):
    """Return a mapping from expected lowercase name -> actual column name in df (case-insensitive)."""
    cols_lower = {c.lower(): c for c in df.columns}
    mapping = {}
    for exp in EXPECTED_COLUMNS:
        if exp in cols_lower:
            mapping[exp] = cols_lower[exp]
    return mapping

def safe_read_csv(path, verbose=False):
    if verbose:
        print(f"Reading CSV: {path}")
    try:
        df = pd.read_csv(path)
    except Exception as e:
        raise RuntimeError(f"Failed to read CSV '{path}': {e}")
    if verbose:
        print(f"Loaded {len(df)} rows, columns: {list(df.columns)}")
    return df

def coerce_numeric(series, name, verbose=False):
    coerced = pd.to_numeric(series, errors='coerce')
    n_bad = coerced.isna().sum()
    if verbose and n_bad:
        print(f"Column '{name}': {n_bad} non-numeric or missing values coerced to NaN")
    return coerced

def visualize_metrics(csv_file: str, out_file: str = None, show: bool = False, no_save: bool = False, verbose: bool = False):
    csv_path = Path(csv_file)
    if not csv_path.exists():
        raise FileNotFoundError(f"File not found: {csv_file}")

    df = safe_read_csv(csv_path, verbose=verbose)
    col_map = find_columns(df)

    # Check required columns
    missing = [c for c in EXPECTED_COLUMNS if c not in col_map]
    if missing:
        raise KeyError(f"Missing required columns in CSV (case-insensitive match): {missing}. Found columns: {list(df.columns)}")

    # Work with consistent names
    df = df.rename(columns={col_map[k]: k.capitalize() for k in col_map})  # Cost, Latency, Eventtype, Successful
    # Ensure canonical names
    df_cols = {c.lower(): c for c in df.columns}

    # Coerce numeric columns
    df['Cost'] = coerce_numeric(df[df_cols['cost']], 'Cost', verbose=verbose)
    df['Latency'] = coerce_numeric(df[df_cols['latency']], 'Latency', verbose=verbose)

    # Event type and success
    event_col = df_cols.get('eventtype')
    success_col = df_cols.get('successful')

    # If Successful is non-boolean, try to coerce (0/1, True/False)
    if success_col:
        if df[success_col].dtype == object:
            # attempt numeric coercion
            df['Successful'] = pd.to_numeric(df[success_col], errors='coerce')
        else:
            df['Successful'] = df[success_col].astype('float')
    else:
        # fallback: create 'Successful' as all 1s (if not present)
        df['Successful'] = 1.0

    # Drop rows where Cost or Latency are NaN (can't plot them)
    before = len(df)
    df = df.dropna(subset=['Cost', 'Latency'])
    dropped = before - len(df)
    if verbose and dropped:
        print(f"Dropped {dropped} rows due to NaN Cost/Latency")

    if len(df) == 0:
        raise ValueError("No valid rows remain after coercion/dropping NaNs for Cost/Latency.")

    # Prepare figure
    fig, axes = plt.subplots(2, 2, figsize=(14, 10))
    fig.suptitle('UAV-MEC Simulation Metrics Analysis', fontsize=16, fontweight='bold')

    # Plot 1: Cost distribution (use seaborn histplot if available)
    try:
        sns.histplot(df['Cost'], bins=50, ax=axes[0, 0], kde=False)
    except Exception:
        axes[0, 0].hist(df['Cost'], bins=50)
    axes[0, 0].set_title('Cost Distribution')
    axes[0, 0].set_xlabel('Cost ($)')
    axes[0, 0].set_ylabel('Frequency')

    # Plot 2: Latency distribution
    try:
        sns.histplot(df['Latency'], bins=50, ax=axes[0, 1], kde=False)
    except Exception:
        axes[0, 1].hist(df['Latency'], bins=50)
    axes[0, 1].set_title('Latency Distribution')
    axes[0, 1].set_xlabel('Latency (ms)')
    axes[0, 1].set_ylabel('Frequency')

    # Plot 3: Event type counts
    event_counts = df[event_col].value_counts() if event_col else pd.Series(dtype=int)
    axes[1, 0].bar(event_counts.index.astype(str), event_counts.values)
    axes[1, 0].set_title('Event Type Distribution')
    axes[1, 0].set_ylabel('Count')
    axes[1, 0].tick_params(axis='x', rotation=45)

    # Plot 4: Cost vs Latency scatter
    axes[1, 1].scatter(df['Cost'], df['Latency'], alpha=0.6, s=20)
    axes[1, 1].set_title('Cost vs Latency')
    axes[1, 1].set_xlabel('Cost ($)')
    axes[1, 1].set_ylabel('Latency (ms)')

    plt.tight_layout(rect=[0, 0.03, 1, 0.95])

    # Determine output file
    if out_file:
        out_path = Path(out_file)
    else:
        out_path = csv_path.with_name(csv_path.stem + "_analysis.png")

    if not no_save:
        plt.savefig(out_path, dpi=300, bbox_inches='tight')
        if verbose:
            print(f"Saved visualization to {out_path}")

    if show:
        plt.show()

    plt.close(fig)

    # Compute statistics
    cost_mean = df['Cost'].mean()
    cost_std = df['Cost'].std()
    latency_mean = df['Latency'].mean()
    latency_max = df['Latency'].max()
    # Success rate: guard against zero-length
    success_rate = None
    if len(df) > 0 and 'Successful' in df.columns:
        # treat any non-zero as success
        successes = df['Successful'].fillna(0).astype(float)
        denom = len(successes)
        if denom > 0:
            success_rate = (successes.sum() / denom) * 100.0

    stats = {
        "rows": len(df),
        "cost_mean": cost_mean,
        "cost_std": cost_std,
        "latency_mean": latency_mean,
        "latency_max": latency_max,
        "success_rate_pct": success_rate
    }

    if verbose:
        print("\n=== Statistics ===")
        print(f"Rows (after cleaning): {stats['rows']}")
        print(f"Cost - Mean: ${stats['cost_mean']:.4f}, Std: ${stats['cost_std']:.4f}")
        print(f"Latency - Mean: {stats['latency_mean']:.2f} ms, Max: {stats['latency_max']:.2f} ms")
        if success_rate is not None:
            print(f"Success Rate: {stats['success_rate_pct']:.2f}%")
        else:
            print("Success Rate: n/a")

    return stats

def main():
    p = argparse.ArgumentParser(description="Visualize UAV-MEC simulation metrics from CSV")
    p.add_argument('csv_file', help='Path to metrics CSV file')
    p.add_argument('--out', '-o', help='Output image file (PNG)', default=None)
    p.add_argument('--show', action='store_true', help='Show plot interactively')
    p.add_argument('--no-save', action='store_true', help='Do not save output image file')
    p.add_argument('--verbose', '-v', action='store_true', help='Verbose logging')
    args = p.parse_args()

    try:
        stats = visualize_metrics(
            csv_file=args.csv_file,
            out_file=args.out,
            show=args.show,
            no_save=args.no_save,
            verbose=args.verbose
        )
        sys.exit(0)
    except Exception as e:
        print(f"ERROR: {e}", file=sys.stderr)
        if args.verbose:
            import traceback
            traceback.print_exc()
        sys.exit(2)

if __name__ == '__main__':
    main()
