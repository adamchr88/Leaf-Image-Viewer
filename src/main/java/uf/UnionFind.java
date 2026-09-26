package uf;

public class UnionFind {

    // parent[i] tells us the parent of node i in the tree
    private int[] parent;

    // size[i] stores how many elements are in the set whose root is i
    private int[] size;

    // Constructor: creates n separate sets (each element is its own set)
    public UnionFind(int n) {

        parent = new int[n];
        size = new int[n];

        // Initially every element is its own parent (separate sets)
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    // Finds the root of element x
    public int find(int x) {

        // If x is not the root, follow parent pointers up the tree
        if (parent[x] != x) {

            // Path compression:
            // directly connect x to the root to make future lookups faster
            parent[x] = find(parent[x]);
        }

        return parent[x];
    }

    // Merges the sets containing a and b
    public void union(int a, int b) {

        int rootA = find(a);
        int rootB = find(b);

        // If they are already in the same set, do nothing
        if (rootA == rootB) {
            return;
        }

        // Union by size:
        // attach the smaller tree under the larger one
        if (size[rootA] < size[rootB]) {

            parent[rootA] = rootB;
            size[rootB] += size[rootA];

        } else {

            parent[rootB] = rootA;
            size[rootA] += size[rootB];
        }
    }

    // Returns the size of the set that x belongs to
    public int getSize(int x) {

        // Find the root and return its size
        return size[find(x)];
    }
}