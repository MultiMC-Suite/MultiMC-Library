package fr.xen0xys.multimc.common.utils;

public record Tuple<K, V>(K first, V second) {
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Tuple<?, ?> tuple))
            return false;
        return tuple.first().equals(this.first()) && tuple.second().equals(this.second());
    }
}
