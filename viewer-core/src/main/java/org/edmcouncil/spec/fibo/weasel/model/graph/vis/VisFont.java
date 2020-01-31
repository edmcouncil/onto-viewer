package org.edmcouncil.spec.fibo.weasel.model.graph.vis;

class VisFont {

    private int size;

    VisFont(int size) {
        this.size = size;
    }

    public static VisFont createDefault() {
        return new VisFont(15);
    }

    public int getSize() {
        return size;
    }

}
