package ru.sbrf.wallet.command;

import android.os.Looper;
import android.test.AndroidTestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandTest extends AndroidTestCase {

    public void  testExecuteCommand(){

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger res = new AtomicInteger();

        CommandExecutor executor = CommandExecutor.getInstance();
        assertNotNull(executor);

        executor.exeTask(new MutlCommand(7, 5), new CommandCompletedListener<CommandStatus<?>>() {
            @Override
            public void onCommandComplete(Command<CommandStatus<?>> mCommand, CommandStatus<?> result) {
                assertTrue(Looper.myLooper() == Looper.getMainLooper());
                assertNotNull(result);
                res.set(((CommandStatus.OK<Integer>) result).getData());
                latch.countDown();
            }

            @Override
            public void onCommandCancelled(Command cmd) {

            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(35, res.intValue());
    }



    public void testCancelCommand(){

        final CountDownLatch latch = new CountDownLatch(1);

        CommandExecutor executor = CommandExecutor.getInstance();
        assertNotNull(executor);

        final LongCommand command = new LongCommand(4);

        executor.exeTask(command, new CommandCompletedListener() {
            @Override
            public void onCommandComplete(Command mCommand, Object result) {
                assertTrue("not run", false);
            }

            @Override
            public void onCommandCancelled(Command cmd) {
                assertEquals(cmd, command);
                assertTrue(command.isCancel());
                latch.countDown();
            }
        });

        executor.cancelAll();


        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(command.isCancel());

    }


}
