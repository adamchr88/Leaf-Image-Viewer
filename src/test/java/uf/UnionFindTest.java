package uf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UnionFindTest {

    @Test
    public void testFindInitiallyPointsToSelf() {

        UnionFind uf = new UnionFind(5);

        for (int i = 0; i < 5; i++) {
            assertEquals(i, uf.find(i));
        }
    }

    @Test
    public void testUnionConnectsNodes() {

        UnionFind uf = new UnionFind(5);

        uf.union(1, 2);

        assertEquals(uf.find(1), uf.find(2));
    }

    @Test
    public void testUnionBySize() {

        UnionFind uf = new UnionFind(5);

        uf.union(1, 2);
        uf.union(2, 3);

        assertEquals(3, uf.getSize(1));
    }

    @Test
    public void testSeparateSetsRemainSeparate() {

        UnionFind uf = new UnionFind(5);

        uf.union(1, 2);
        uf.union(3, 4);

        assertNotEquals(uf.find(1), uf.find(3));
    }
}