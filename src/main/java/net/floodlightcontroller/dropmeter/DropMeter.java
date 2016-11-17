package net.floodlightcontroller.dropmeter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMeterFlags;
import org.projectfloodlight.openflow.protocol.OFMeterMod;
import org.projectfloodlight.openflow.protocol.OFMeterModCommand;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionMeter;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBand;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBandDrop;
import org.projectfloodlight.openflow.types.OFPort;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.routing.Path;

public class DropMeter{

	    public void createMeter(IOFSwitch sw) {
	    	 OFFactory meterFactory = OFFactories.getFactory(OFVersion.OF_13);
	            OFMeterMod.Builder meterModBuilder = meterFactory.buildMeterMod()
	                .setMeterId(meterid).setCommand(OFMeterModCommand.ADD);

	            int rate  = 40000; 
	            OFMeterBandDrop.Builder bandBuilder = meterFactory.meterBands().buildDrop()
	                .setRate(rate);
	            OFMeterBand band = bandBuilder.build();
	            List<OFMeterBand> bands = new ArrayList<OFMeterBand>();
	            bands.add(band);
	  
	            Set<OFMeterFlags> flags2 = new HashSet<>();
	            flags2.add(OFMeterFlags.KBPS);
	            meterModBuilder.setMeters(bands)
	                .setFlags(flags2).build();

	            sw.write(meterModBuilder.build());
	     }
	    
	    public void bindMeterWithFlow(IOFSwitch sw, OFPort srcPort, Path path) {
	    	Match.Builder mb = sw.getOFFactory().buildMatch();
            mb.setExact(MatchField.IN_PORT, srcPort);

            OFFactory my13Factory = OFFactories.getFactory(OFVersion.OF_13);
            ArrayList<OFInstruction> instructions = new ArrayList<OFInstruction>();
            ArrayList<OFAction> actionList = new ArrayList<OFAction>();
            OFInstructionMeter meter = my13Factory.instructions().buildMeter()
                .setMeterId(meterid)
                .build();
            OFActionOutput output = my13Factory.actions().buildOutput()
                .setPort(path.getPath().get(1).getPortId())
                .build();

            actionList.add(output);
            OFInstructionApplyActions applyActions = my13Factory.instructions().buildApplyActions()
                .setActions(actionList)
                .build();
            instructions.add(applyActions);
            instructions.add(meter);
            meterid++;

            OFFlowAdd flowAdd = my13Factory.buildFlowAdd()
                    .setMatch(mb.build())
                    .setInstructions(instructions)
                    .setPriority(32768)
                    .build();
                sw.write(flowAdd);
	    }
	    
	    protected static int meterid = 1; 
	  
}
