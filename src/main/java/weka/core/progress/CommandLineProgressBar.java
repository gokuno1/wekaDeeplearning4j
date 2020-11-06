package weka.core.progress;

import lombok.extern.log4j.Log4j2;

import java.util.Timer;
import java.util.TimerTask;

@Log4j2
/**
 * Command line implementation of a progress bar
 */
public class CommandLineProgressBar extends AbstractProgressBar {

    public CommandLineProgressBar(double maxProgress, String progressMessage) {
        super(maxProgress, progressMessage);
    }

    /** Total number of characters across the progress bar */
    protected int totalProgressBarSize = 60;

    /** Size of the indeterminate bar which scrolls across */
    private int indeterminateBarSize = 10;

    /** Number of Progress chars to print */
    private int currNumDots = 0;

    /** Space to the left and right of the progress bar */
    private int leftSpace = 0, rightSpace = 0;

    /** Timer for refreshing the indeterminate progress bar */
    protected Timer timer;

    /**
     * Update the progress bar with the current value
     */
    @Override
    protected void onSetProgress() {
        if (m_indeterminate) {
            // The indeterminate bar is a fixed size that 'scrolls' across the terminal
            // There are 3 components, leftSpace, numDots, rightSpace
            // e.g., [  ==========   ] - leftSpace = 2, numDots = 10, rightSpace = 3
            int progressInBar = (int) m_actualProgress % totalProgressBarSize;
            leftSpace = progressInBar - indeterminateBarSize;
            leftSpace = Math.max(0, leftSpace); // Don't let it go below 0

            if (progressInBar <= indeterminateBarSize) {
                currNumDots = progressInBar;
            } else {
                currNumDots = indeterminateBarSize;
            }

            rightSpace = totalProgressBarSize - progressInBar;
        } else {
            leftSpace = 0;
            currNumDots = (int) (m_normalizedProgress * totalProgressBarSize);
            rightSpace = totalProgressBarSize - currNumDots;
        }
    }

    /**
     * Update the progress bar display
     */
    @Override
    public void refreshDisplay() {
        String progressChar = "=";
        String progressRemainingChar = " ";

        System.err.printf("\r%s: [%s%s%s] %s",
                getProgressMessage(),
                progressRemainingChar.repeat(leftSpace),
                progressChar.repeat(currNumDots),
                progressRemainingChar.repeat(rightSpace),
                getETAString());
    }

    /**
     * Do any required setup on start (e.g., show a popup window)
     */
    @Override
    protected void onStart() {
        System.err.println("\n\n");
        if (m_indeterminate) {
            m_actualProgress = 0;
            startIndeterminateLoader();
        }
    }

    /** Start a new timer which repeatedly increments the indeterminate progress bar loader */
    private void startIndeterminateLoader() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                increment();
            }
        }, 0, 250);
    }

    /**
     * Perform any teardown (e.g., close popup window, display completion message)
     */
    @Override
    public void finish() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        System.err.println("\nDone!\n");
    }
}