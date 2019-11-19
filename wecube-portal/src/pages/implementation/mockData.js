export const modelData = [
  {
    id: "UNIT_APP",
    srcGraphNodeIds: [],
    succeedingNodeIds: ["INS1", "INS2", "PACK_V1"],
    refFlowNodeIds: []
  },
  {
    id: "INS1",
    srcGraphNodeIds: ["UNIT_APP"],
    succeedingNodeIds: ["HOST1", "PACK_V1"],
    refFlowNodeIds: []
  },
  {
    id: "HOST1",
    srcGraphNodeIds: ["INS1"],
    succeedingNodeIds: ["DISK1", "IP1"],
    refFlowNodeIds: []
  },
  {
    id: "DISK1",
    srcGraphNodeIds: ["HOST1"],
    succeedingNodeIds: [],
    refFlowNodeIds: []
  },
  {
    id: "IP1",
    srcGraphNodeIds: ["HOST1"],
    succeedingNodeIds: [],
    refFlowNodeIds: []
  },
  {
    id: "PACK_V1",
    srcGraphNodeIds: ["INS1", "UNIT_APP", "INS2"],
    succeedingNodeIds: ["USER"],
    refFlowNodeIds: []
  },
  {
    id: "USER",
    srcGraphNodeIds: ["PACK_V1"],
    succeedingNodeIds: [],
    refFlowNodeIds: []
  },
  {
    id: "INS2",
    srcGraphNodeIds: ["UNIT_APP"],
    succeedingNodeIds: ["HOST2", "PACK_V1"],
    refFlowNodeIds: []
  },
  {
    id: "HOST2",
    srcGraphNodeIds: ["INS2"],
    succeedingNodeIds: ["IP2", "DISK2"],
    refFlowNodeIds: []
  },
  {
    id: "IP2",
    srcGraphNodeIds: ["HOST2"],
    succeedingNodeIds: [],
    refFlowNodeIds: []
  },
  {
    id: "DISK2",
    srcGraphNodeIds: ["HOST2"],
    succeedingNodeIds: [],
    refFlowNodeIds: []
  }
];
