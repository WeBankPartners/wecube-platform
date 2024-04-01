package tools

type graph struct {
	vertex int           //顶点
	list   map[int][]int //连接表边
}

// 添加边
func (g *graph) addVertex(t int, s int) {
	g.list[t] = push(g.list[t], s)
}

// KahnSort 拓扑排序
func (g *graph) KahnSort() map[int]int {
	// 入度
	var inDegree = make(map[int]int)
	// 储存结果的队列
	var queue []int

	// 增加入度
	for i := 1; i <= g.vertex; i++ {
		for _, m := range g.list[i] {
			inDegree[m]++
		}
	}

	//如果某个节点的入度为0，就加入结果队列
	for i := 0; i <= g.vertex; i++ {
		if inDegree[i] == 0 {
			queue = push(queue, i)
		}
	}

	//当有值加入结果队列
	result := make(map[int]int)
	resultIndex := 0
	for len(queue) > 0 {
		var now int
		now, queue = pop(queue)
		result[now] = resultIndex
		resultIndex += 1
		// 将所有相邻的入度减1
		for _, k := range g.list[now] {
			inDegree[k]--
			//如果相邻点的入度恰好变为0，加入结果队列中
			if inDegree[k] == 0 {
				queue = push(queue, k)
			}
		}
	}
	return result
}

// NewGraph 创建图
func newGraph(v int) *graph {
	g := new(graph)
	g.vertex = v
	g.list = map[int][]int{}
	i := 0
	for i < v {
		g.list[i] = make([]int, 0)
		i++
	}
	return g
}

// 取出切片第一个元素
func pop(list []int) (int, []int) {
	if len(list) > 0 {
		a := list[0]
		b := list[1:]
		return a, b
	} else {
		return -1, list
	}
}

// 推入切片
func push(list []int, value int) []int {
	result := append(list, value)
	return result
}

func ProcNodeSort(nodeIds []string, links [][]string) (nodeIndexMap map[string]int, isLoop bool) {
	nodeIndexMap = make(map[string]int)
	curNodeIndexMap := make(map[string]int)
	for i, v := range nodeIds {
		curNodeIndexMap[v] = i + 1
	}
	g := newGraph(len(nodeIds))
	for _, link := range links {
		if len(link) != 2 {
			continue
		}
		g.addVertex(curNodeIndexMap[link[0]], curNodeIndexMap[link[1]])
	}
	indexMap := g.KahnSort()
	for k, v := range curNodeIndexMap {
		sortIndex := indexMap[v]
		if sortIndex == 0 {
			isLoop = true
			break
		}
		nodeIndexMap[k] = sortIndex
	}
	return
}
