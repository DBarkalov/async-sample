package ru.sbrf.wallet.command;

import junit.framework.TestCase;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class CommandGroupFlowTestBase extends TestCase {

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        CommandGroup.setSingleCommandExecutor(new CommandGroup.DefaultSingleCommandExecutor());
    }

    public void testGroupFlow() {
        //set most result for command group
        LinkedHashMap<Class<?> ,CommandStatus<?>> mockResults = new LinkedHashMap<>();
        mockResults.put(SummCommand.class, new CommandStatus.OK<> (44));
        mockResults.put(MutlCommand.class, new CommandStatus.OK<> (22));
        MockSingleCommandExecutor mock = new MockSingleCommandExecutor(mockResults);
        CommandGroup.setSingleCommandExecutor(mock);

        //expected
        LinkedList<Class<?>> flow = new LinkedList<>();
        flow.add(SummCommand.class);
        flow.add(MutlCommand.class);

        RealGroupMock commandGroup = new RealGroupMock();
        commandGroup.execute();

        assertCommamdGroupFlow(flow, commandGroup.getFlow());
    }

    private void assertCommamdGroupFlow(LinkedList<Class<?>> flowExpected, LinkedList<Class<?>> flow) {
        assertEquals("size expect = " +  flowExpected.size() + " real=" + flow.size(), flowExpected.size(), flow.size());
        assertTrue(flowExpected.equals(flow));
    }

    /**
     * real command example
     */
    public static class RealGroup extends CommandGroup {

        public RealGroup(){
            addCommand(new SummCommand(2,3));
        }

        @Override
        protected void onCommandExecuted(Command<?> command, CommandStatus<?> result) {
            // logic example
            if(command instanceof SummCommand){
                addCommand(new MutlCommand(1,4));
            }
        }
    }

    /**
     *  command wrapper for flow test
     */
    public static class RealGroupMock extends RealGroup {

        LinkedList<Class<?>> flow = new LinkedList<>();

        @Override
        protected void onCommandExecuted(Command<?> command, CommandStatus<?> result) {
            super.onCommandExecuted(command, result);
            flow.add(command.getClass());
        }

        public LinkedList<Class<?>> getFlow() {
            return flow;
        }
    }



}
