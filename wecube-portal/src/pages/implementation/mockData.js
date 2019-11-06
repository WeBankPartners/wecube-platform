export const modelData = [
  {
    id: "UNIT_APP",
    srcGraphNodeIds: [],
    toGraphNodeIds: ["INS1", "INS2", "PACK_V1"],
    refFlowNodeIds: []
  },
  {
    id: "INS1",
    srcGraphNodeIds: ["UNIT_APP"],
    toGraphNodeIds: ["HOST1", "PACK_V1"],
    refFlowNodeIds: []
  },
  {
    id: "HOST1",
    srcGraphNodeIds: ["INS1"],
    toGraphNodeIds: ["DISK1", "IP1"],
    refFlowNodeIds: []
  },
  {
    id: "DISK1",
    srcGraphNodeIds: ["HOST1"],
    toGraphNodeIds: [],
    refFlowNodeIds: []
  },
  {
    id: "IP1",
    srcGraphNodeIds: ["HOST1"],
    toGraphNodeIds: [],
    refFlowNodeIds: []
  },
  {
    id: "PACK_V1",
    srcGraphNodeIds: ["INS1", "UNIT_APP", "INS2"],
    toGraphNodeIds: ["USER"],
    refFlowNodeIds: []
  },
  {
    id: "USER",
    srcGraphNodeIds: ["PACK_V1"],
    toGraphNodeIds: [],
    refFlowNodeIds: []
  },
  {
    id: "INS2",
    srcGraphNodeIds: ["UNIT_APP"],
    toGraphNodeIds: ["HOST2", "PACK_V1"],
    refFlowNodeIds: []
  },
  {
    id: "HOST2",
    srcGraphNodeIds: ["INS2"],
    toGraphNodeIds: ["IP2", "DISK2"],
    refFlowNodeIds: []
  },
  {
    id: "IP2",
    srcGraphNodeIds: ["HOST2"],
    toGraphNodeIds: [],
    refFlowNodeIds: []
  },
  {
    id: "DISK2",
    srcGraphNodeIds: ["HOST2"],
    toGraphNodeIds: [],
    refFlowNodeIds: []
  }
];

export const flowData = [
  {
    id: 0,
    name: "Start",
    status: "NotStarted",
    toGraphNodeIds: [1],
    refGraphNodeIds: []
  },
  {
    id: 1,
    name: "Create VM",
    toGraphNodeIds: [2],
    status: "NotStarted",
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 2,
    name: "Create Disk",
    status: "NotStarted",
    toGraphNodeIds: [3],
    refGraphNodeIds: ["DISK1", "DISK2"]
  },
  {
    id: 3,
    name: "Install saltstack agent",
    status: "NotStarted",
    toGraphNodeIds: [4],
    refGraphNodeIds: ["INS1", "INS2"]
  },
  {
    id: 4,
    name: "Install monitor agent",
    status: "NotStarted",
    toGraphNodeIds: [5],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 5,
    name: "Create Deploy User",
    status: "NotStarted",
    toGraphNodeIds: [6],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 6,
    name: "Differential variable replacement",
    status: "NotStarted",
    toGraphNodeIds: [7],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 7,
    name: "Artifacts distribution",
    status: "NotStarted",
    toGraphNodeIds: [8],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 8,
    name: "Deployment",
    status: "NotStarted",
    toGraphNodeIds: [9],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 9,
    name: "Start up",
    status: "NotStarted",
    toGraphNodeIds: [10],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 10,
    name: "End",
    toGraphNodeIds: [],
    refGraphNodeIds: []
  }
];

export const allFlows = [
  {
    id: 1,
    orchestration: {
      orchestrationId: 1001,
      orchestrationName: "App Deployment"
    },
    target: {
      targetId: 2001,
      targetName: "Deposit System - Core Subsystem - APP Unit"
    },
    timestamp: "2018.01.01 12:22:23",
    createBy: "Chaney Liu"
  },
  {
    id: 2,
    orchestration: {
      orchestrationId: 1002,
      orchestrationName: "Create Server Farm Zone"
    },
    target: {
      targetId: 2002,
      targetName: "SZ1-DMZ"
    },
    timestamp: "2018.12.12 16:44:23",
    createBy: "Tong Zhang"
  }
];
