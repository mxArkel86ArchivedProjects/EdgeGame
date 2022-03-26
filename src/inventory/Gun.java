package inventory;

public abstract class Gun extends Weapon {
    public Magazine mag;

    Gun() {
        
    }

    public abstract double DAMAGE();

    public abstract boolean DESTROY_ON_SURFACE();

    public abstract String MAG_TYPE();

    public abstract int FIRING_TYPE();

    public abstract int FIRING_DELAY();

    public abstract int RELOAD_DELAY();

    public abstract int BULLET_SIZE();
}
