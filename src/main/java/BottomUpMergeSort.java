public class BottomUpMergeSort {
    private int[] aux;

    public void sort(int[] a) {
        int n = a.length;
        aux = new int[n];
        for (int size = 1; size < n; size *= 2) {
            for (int lo = 0; lo < n - size; lo += size * 2) {
                int mid = lo + size - 1;
                int hi = Math.min(lo + size * 2 - 1, n - 1);
                merge(a, lo, mid, hi);
            }
        }
    }

    private void merge(int[] a, int lo, int mid, int hi) {
        for (int k = lo; k <= hi; k++) {
            aux[k] = a[k];
        }
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                a[k] = aux[j++];
            } else if (j > hi) {
                a[k] = aux[i++];
            } else if (aux[j] < aux[i]) {
                a[k] = aux[j++];
            } else {
                a[k] = aux[i++];
            }
        }
    }
}