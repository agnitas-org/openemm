package com.agnitas.testsupport;

import org.easymock.EasyMockSupport;
import org.easymock.IMocksControl;
import org.easymock.internal.MocksControl;
import org.easymock.internal.ReplayState;

public class EmmEasyMockSupport extends EasyMockSupport {

    public boolean isInReplayState() {
        for (IMocksControl control : controls) {
            if (control instanceof MocksControl mocksControl && mocksControl.getState() instanceof ReplayState) {
                return true;
            }
        }

        return false;
    }

    public void addMockControl(IMocksControl mocksControl) {
        controls.add(mocksControl);
    }

}
