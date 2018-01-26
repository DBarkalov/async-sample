package ru.sbrf.wallet.command;

import android.test.AndroidTestCase;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

public class CommandGroupTest extends AndroidTestCase {

    private static final String TAG = "CommandGroupTest";

    public void testRunGroup (){
        Group1 cmd = new Group1();
        cmd.execute();
        assertEquals(60, cmd.getSumm());
    }

    public void testCancelGroup(){

        Log.d(TAG, "start test");

        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch stopLatch = new CountDownLatch(1);

        CommandExecutor executor = CommandExecutor.getInstance();
        assertNotNull(executor);

        final LongCommand cmd1 = new MockLongCommand(2, startLatch);
        final LongCommand cmd2 = new LongCommand(5);
        final LongCommand cmd3 = new LongCommand(4);
        final SummGroup group2 = new SummGroup(cmd1, cmd2, cmd3);

        executor.exeTask(group2, new CommandCompletedListener() {
            @Override
            public void onCommandComplete(Command mCommand, Object result) {
                Log.d(TAG, "onCommandComplete");
                assertTrue("not run", false);
            }

            @Override
            public void onCommandCancelled(Command cmd) {
                Log.d(TAG, "onCommandCancelled");
                assertTrue(cmd.isCancel());
                stopLatch.countDown();
            }
        });

        try {
            startLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.cancelAll();

        try {
            stopLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "end test");

        assertTrue(group2.isCancel());

        assertTrue(cmd1.isCancel());
        assertFalse(cmd2.isCancel());
        assertFalse(cmd3.isCancel());

        assertTrue(cmd1.isExecuted());
        assertFalse(cmd2.isExecuted());
        assertFalse(cmd3.isExecuted());
    }

    private static class Group1 extends CommandGroup {

        private int summ;

        public Group1(){
            addCommand(new MutlCommand(3, 4));
            addCommand(new MutlCommand(8, 1));
            addCommand(new MutlCommand(5, 8));
        }

        @Override
        protected void onCommandExecuted(Command<?> command, CommandStatus<?> result) {

            if(command instanceof MutlCommand && result != null){
                int r = ((CommandStatus.OK<Integer>)result).getData().intValue();
                summ+=r;
            }
        }

        public int getSumm() {
            return summ;
        }

    }

    public static class MockLongCommand extends LongCommand {

        CountDownLatch startLatch;

        public MockLongCommand(int m, CountDownLatch startLatch) {
            super(m);
            this.startLatch = startLatch;
        }

        @Override
        public CommandStatus<Integer> execute() {
            Log.d(TAG, "MockLongCommand execute()");
            startLatch.countDown();
            return super.execute();
        }
    }

    public static class SummGroup extends CommandGroup {

        public SummGroup(CancelledCommand<?>... command) {
            super(command);
        }

        private int summ;

        @Override
        protected void onCommandExecuted(Command<?> command, CommandStatus<?> result) {
            Log.d(TAG, "onCommandExecuted " + command.getClass().getSimpleName());
            if (command instanceof LongCommand && result.isOK()) {
                summ += ((Integer) result.getData()).intValue();
            }
        }
    }

}
