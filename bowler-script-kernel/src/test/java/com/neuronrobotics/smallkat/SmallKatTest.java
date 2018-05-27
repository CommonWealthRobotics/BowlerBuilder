/*
 * Copyright 2015 Kevin Harrington
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.neuronrobotics.smallkat;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import org.junit.jupiter.api.Test;

public class SmallKatTest {

  @Test
  void loadCreatureFromScriptingEngine() throws Exception {
    ScriptingEngine.gitScriptRun("https://github.com/keionbis/SmallKat.git", "launch.groovy", null);
  }

  //  @Test
  //  void loadCreatureManually() {
  //    final HIDSimpleComsDevice device =
  //        (HIDSimpleComsDevice)
  //            DeviceManager.getSpecificDevice(
  //                "hidDevice",
  //                () -> {
  //                  HIDSimpleComsDevice simpleComsDevice = new HIDSimpleComsDevice(0x16c0, 0x480);
  //                  simpleComsDevice.connect();
  //                  LinkFactory.addLinkProvider(
  //                      "hidfast",
  //                      conf -> {
  //                        System.out.println("Loading link.");
  //                        return new HIDRotoryLink(simpleComsDevice, conf);
  //                      });
  //
  //                  System.out.println("Connecting new device: " + simpleComsDevice);
  //                  return simpleComsDevice;
  //                });
  //
  //    final MobileBase cat =
  //        (MobileBase)
  //            DeviceManager.getSpecificDevice(
  //                "MediumKat",
  //                () -> {
  //                  try {
  //                    MobileBase mobileBase =
  //                        MobileBaseLoader.fromGit(
  //                            "https://github.com/keionbis/SmallKat.git", "Bowler/MediumKat.xml");
  //
  //                    device.simple.addEvent(
  //                        1804,
  //                        () -> {
  //                          double[] imuDataValues = device.simple.getImuData();
  //                          mobileBase
  //                              .getImu()
  //                              .setHardwareState(
  //                                  new IMUUpdate(
  //                                      imuDataValues[0],
  //                                      imuDataValues[1],
  //                                      imuDataValues[2],
  //                                      imuDataValues[3],
  //                                      imuDataValues[4],
  //                                      imuDataValues[5]));
  //                        });
  //
  //                    if (mobileBase == null)
  //                      throw new RuntimeException("Arm failed to assemble itself");
  //                    System.out.println("Connecting new device to robot arm: " + mobileBase);
  //                    return mobileBase;
  //                  } catch (Exception e) {
  //                    throw new RuntimeException(e);
  //                  }
  //                });
  //  }
  //
  //  private class SimpleServoHID extends HIDSimplePacketComs {
  //    private PacketType servos = new edu.wpi.SimplePacketComs.BytePacketType(1962, 64);
  //    private PacketType imuData = new edu.wpi.SimplePacketComs.FloatPacketType(1804, 64);
  //    private final double[] status = new double[12];
  //    private final byte[] data = new byte[16];
  //
  //    SimpleServoHID(int vidIn, int pidIn) {
  //      super(vidIn, pidIn);
  //      addPollingPacket(servos);
  //      addPollingPacket(imuData);
  //      addEvent(1962, () -> writeBytes(1962, data));
  //      addEvent(1804, () -> readFloats(1804, status));
  //    }
  //
  //    double[] getImuData() {
  //      return status;
  //    }
  //
  //    byte[] getData() {
  //      return data;
  //    }
  //  }
  //
  //  private class HIDSimpleComsDevice extends NonBowlerDevice {
  //
  //    SimpleServoHID simple;
  //
  //    public HIDSimpleComsDevice(int vidIn, int pidIn) {
  //      simple = new SimpleServoHID(vidIn, pidIn);
  //      setScriptingName("hidbowler");
  //    }
  //
  //    @Override
  //    public void disconnectDeviceImp() {
  //      simple.disconnect();
  //      System.out.println("HID device Termination signal shutdown");
  //    }
  //
  //    @Override
  //    public boolean connectDeviceImp() {
  //      return simple.connect();
  //    }
  //
  //    void setValue(int i, int position) {
  //      simple.getData()[i] = (byte) position;
  //    }
  //
  //    int getValue(int i) {
  //      if (simple.getData()[i] > 0) return simple.getData()[i];
  //      return ((int) simple.getData()[i]) + 256;
  //    }
  //
  //    public float[] getImuData() {
  //      double[] doubleArray = simple.getImuData();
  //      float[] floatArray = new float[doubleArray.length];
  //      for (int i = 0; i < doubleArray.length; i++) {
  //        floatArray[i] = (float) doubleArray[i];
  //      }
  //      return floatArray;
  //    }
  //
  //    @Override
  //    public ArrayList<String> getNamespacesImp() {
  //      // no namespaces on dummy
  //      return new ArrayList<>();
  //    }
  //  }
  //
  //  private class HIDRotoryLink extends AbstractRotoryLink {
  //
  //    private HIDSimpleComsDevice device;
  //    private int index;
  //    private int lastPushedVal;
  //    private final Integer command = 1962;
  //
  //    /**
  //     * Instantiates a new HID rotory link.
  //     *
  //     * @param c the c
  //     * @param conf the conf
  //     */
  //    public HIDRotoryLink(HIDSimpleComsDevice c, LinkConfiguration conf) {
  //      super(conf);
  //      index = conf.getHardwareIndex();
  //      device = c;
  //      if (device == null) throw new RuntimeException("Device can not be null");
  //      c.simple.addEvent(
  //          command,
  //          () -> {
  //            int val = (int) getCurrentPosition();
  //            if (lastPushedVal != val) {
  //              fireLinkListener(val);
  //              lastPushedVal = val;
  //            }
  //          });
  //    }
  //
  //    /* (non-Javadoc)
  //     * @see com.neuronrobotics.sdk.addons.kinematics.AbstractLink#cacheTargetValueDevice()
  //     */
  //    @Override
  //    public void cacheTargetValueDevice() {
  //      device.setValue(index, (int) getTargetValue());
  //    }
  //
  //    /* (non-Javadoc)
  //     * @see com.neuronrobotics.sdk.addons.kinematics.AbstractLink#flush(double)
  //     */
  //    @Override
  //    public void flushDevice(double time) {
  //      // auto flushing
  //    }
  //
  //    /* (non-Javadoc)
  //     * @see com.neuronrobotics.sdk.addons.kinematics.AbstractLink#flushAll(double)
  //     */
  //    @Override
  //    public void flushAllDevice(double time) {
  //      // auto flushing
  //    }
  //
  //    /* (non-Javadoc)
  //     * @see com.neuronrobotics.sdk.addons.kinematics.AbstractLink#getCurrentPosition()
  //     */
  //    @Override
  //    public double getCurrentPosition() {
  //      return (double) device.getValue(index);
  //    }
  //  }
}
