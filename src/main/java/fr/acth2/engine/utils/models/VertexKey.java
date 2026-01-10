package fr.acth2.engine.utils.models;

public class VertexKey {
    int pos;
    int tex;
    int norm;

    public VertexKey(int pos, int tex, int norm) {
        this.pos = pos;
        this.tex = tex;
        this.norm = norm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VertexKey)) return false;
        VertexKey v = (VertexKey) o;
        return pos == v.pos && tex == v.tex && norm == v.norm;
    }

    @Override
    public int hashCode() {
        return (pos * 31 + tex) * 31 + norm;
    }
}