package ruoyun.brandeis.edu.mymaps;

import ruoyun.brandeis.edu.mymaps.ProcessButton;

import android.os.Handler;

import java.io.IOException;
import java.util.Random;

public class ProgressGenerator {

    public interface OnCompleteListener {

        public void onComplete() throws IOException;
    }

    private OnCompleteListener mListener;
    private int mProgress;

    public ProgressGenerator(OnCompleteListener listener) {
        mListener = listener;
    }

    public void start(final ProcessButton button) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgress += 10;
                button.setProgress(mProgress);
                if (mProgress < 100) {
                    handler.postDelayed(this, generateDelay());
                } else {
                    try {
                        mListener.onComplete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, generateDelay());
    }

    private Random random = new Random();

    private int generateDelay() {
        return random.nextInt(1000);
    }
}
