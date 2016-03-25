package com.eje_c.vrvideoplayer;

import com.eje_c.meganekko.Meganekko;
import com.eje_c.meganekko.MeganekkoApp;

public class MainActivity extends BaseActivity {
    @Override
    public MeganekkoApp createMeganekkoApp(Meganekko meganekko) {
        return new VideoPlayerApp(meganekko, this);
    }
}
