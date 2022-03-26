package inventory;

public abstract class Magazine extends Item {
    public int bullet_count;

    Magazine() {
        bullet_count = BULLET_MAX();
    }

    public abstract int BULLET_MAX();

    public abstract String NAME();

    public abstract double BULLET_INITIAL_SPEED();
}
