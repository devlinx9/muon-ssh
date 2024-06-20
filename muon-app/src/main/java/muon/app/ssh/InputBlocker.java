/**
 *
 */
package muon.app.ssh;

/**
 * @author subhro
 *
 */
public interface InputBlocker {
    
    /**
     * TODO enable to set a little timeout and wait for it to show the dialog and block the screen
     * It will make the screen to blink less.
     */
    void blockInput(Runnable cancellable);

    void unblockInput();
}
