package inventory;

public class ItemAttributes {
    public static Gun WISP() {
        return new Gun() {
            @Override
            public double DAMAGE() {
                return 10;
            }

            @Override
            public boolean DESTROY_ON_SURFACE() {
                return false;
            }

            @Override
            public String MAG_TYPE() {
                return "WISP_MAG";
            }

            @Override
            public int FIRING_TYPE() {
                return 0;
            }

            @Override
            public int FIRING_DELAY() {
                return 200;
            }

            @Override
            public int RELOAD_DELAY() {
                return 800;
            }

            @Override
            public int BULLET_SIZE() {
                return 15;
            }

        };
    }

    public static Gun DevTek_Rifle() {
        return new Gun() {

            @Override
            public double DAMAGE() {
                return 14;
            }

            @Override
            public boolean DESTROY_ON_SURFACE() {
                return true;
            }

            @Override
            public String MAG_TYPE() {
                return "DEVTEK MAG";
            }

            @Override
            public int FIRING_TYPE() {
                return 1;
            }

            @Override
            public int FIRING_DELAY() {
                return 140;
            }

            @Override
            public int RELOAD_DELAY() {
                return 1300;
            }

            @Override
            public int BULLET_SIZE() {
                return 20;
            }

        };
    }
    
    public static Magazine DevTek_Mag() {
        return new Magazine() {

            @Override
            public int BULLET_MAX() {
                return 24;
            }

            @Override
            public String NAME() {
                return "DEVTEK MAG";
            }

            @Override
            public double BULLET_INITIAL_SPEED() {
                return 1;
            }
            
        };
    }

    public static Gun THUNDER() {
        return new Gun() {
            @Override
            public double DAMAGE() {
                return 50;
            }

            @Override
            public boolean DESTROY_ON_SURFACE() {
                return true;
            }

            @Override
            public String MAG_TYPE() {
                return "THUNDER MAG";
            }

            @Override
            public int FIRING_TYPE() {
                return 0;
            }

            @Override
            public int FIRING_DELAY() {
                return 1400;
            }

            @Override
            public int RELOAD_DELAY() {
                return 2400;
            }

            @Override
            public int BULLET_SIZE() {
                return 40;
            }

        };
    }

    public static Magazine THUNDER_MAG() {
        return new Magazine() {

            @Override
            public int BULLET_MAX() {
                return 4;
            }

            @Override
            public String NAME() {
                return "THUNDER MAG";
            }

            @Override
            public double BULLET_INITIAL_SPEED() {
                return 0.25;
            }
        };
    }

    public static Magazine WISP_MAG() {
        return new Magazine() {

            @Override
            public int BULLET_MAX() {
                return 12;
            }

            @Override
            public String NAME() {
                return "WISP MAG";
            }

            @Override
            public double BULLET_INITIAL_SPEED() {
                return 1.1;
            }
        };
    }
}
