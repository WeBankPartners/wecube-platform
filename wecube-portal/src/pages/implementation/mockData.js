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
    name: "开始",
    status: "Completed",
    toGraphNodeIds: [1],
    refGraphNodeIds: []
  },
  {
    id: 1,
    name: "创建虚拟机",
    toGraphNodeIds: [2],
    status: "Completed",
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 2,
    name: "创建挂载磁盘",
    status: "Completed",
    toGraphNodeIds: [3],
    refGraphNodeIds: ["DISK1", "DISK2"]
  },
  {
    id: 3,
    name: "安装 saltstack agent",
    status: "NotStarted",
    toGraphNodeIds: [4],
    refGraphNodeIds: ["INS1", "INS2"]
  },
  {
    id: 4,
    name: "安装监控agent",
    status: "NotStarted",
    toGraphNodeIds: [5],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 5,
    name: "创建部署用户",
    status: "NotStarted",
    toGraphNodeIds: [6],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 6,
    name: "差异化变量替换",
    status: "NotStarted",
    toGraphNodeIds: [7],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 7,
    name: "物料包分发解压",
    status: "NotStarted",
    toGraphNodeIds: [8],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 8,
    name: "执行部署脚本",
    status: "NotStarted",
    toGraphNodeIds: [9],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 9,
    name: "执行启动脚本",
    status: "NotStarted",
    toGraphNodeIds: [10],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 10,
    name: "启动应用监控",
    status: "NotStarted",
    toGraphNodeIds: [11],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 11,
    name: "结束",
    toGraphNodeIds: [],
    refGraphNodeIds: []
  }
];

export const allFlows = [
  {
    id: 1,
    orchestration: {
      orchestrationId: 1001,
      orchestrationName: "应用部署"
    },
    target: {
      targetId: 2001,
      targetName: "存款系统-核心子系统-APP单元"
    },
    timestamp: "2018.01.01 12:22:23",
    createBy: "刘超"
  },
  {
    id: 2,
    orchestration: {
      orchestrationId: 1002,
      orchestrationName: "安全区域创建"
    },
    target: {
      targetId: 2002,
      targetName: "SZ1-DMZ"
    },
    timestamp: "2018.12.12 16:44:23",
    createBy: "张桐"
  }
];
