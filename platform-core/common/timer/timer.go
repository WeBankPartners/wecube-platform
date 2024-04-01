package timer

import (
	"fmt"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/try"

	"sync"
	"time"

	"github.com/gorhill/cronexpr"
)

const (
	TIME_WAIT_SEC int64 = 1
)

// func(unix time for seconds, anything)
type TimerFunc func(int64, interface{})

type cronSecondData struct {
	IntervalMilli int64
	CronExpr      *cronexpr.Expression
	NextMilli     int64
	Function      TimerFunc
	FunctionArg   interface{}
}

func (c *cronSecondData) secondNext(intervalMilli int64, nowMilli int64) int64 {
	return intervalMilli - nowMilli%intervalMilli + nowMilli
}

func (c *cronSecondData) cronNext(expr *cronexpr.Expression, nowMilli int64) int64 {
	fromTime := time.Unix(nowMilli/1000, 0)
	nextTime := expr.Next(fromTime)
	if nextTime.IsZero() {
		// never(always schedule after 9999-99-99 23:59:59)
		return nowMilli + 253636876799000
	}
	return nextTime.UnixMilli()
}

func (c *cronSecondData) GetNext(nowMilli int64) int64 {
	if c.CronExpr != nil {
		return c.cronNext(c.CronExpr, nowMilli)
	}
	return c.secondNext(c.IntervalMilli, nowMilli)
}

type CronSecond struct {
	timers     map[string]*cronSecondData
	started    bool
	signStop   chan bool
	timerGuard sync.Mutex
}

func New() *CronSecond {
	c := &CronSecond{timers: make(map[string]*cronSecondData), signStop: make(chan bool, 1)}
	return c
}

func (c *CronSecond) Add(name string, interval int64, function TimerFunc, arg interface{}) error {
	if interval < 1 {
		// return fmt.Errorf("interval must >= 1")
		return fmt.Errorf("interval must >= 1")
	}
	// 转为毫秒级计算精度
	intervalMilli := interval * 1000
	now := time.Now().UTC().UnixMilli()
	item := cronSecondData{IntervalMilli: intervalMilli, Function: function, FunctionArg: arg}
	item.NextMilli = item.GetNext(now)
	c.timerGuard.Lock()
	c.timers[name] = &item
	c.timerGuard.Unlock()
	return nil
}

func (c *CronSecond) GetArg(name string) interface{} {
	c.timerGuard.Lock()
	item, exists := c.timers[name]
	c.timerGuard.Unlock()
	if exists {
		return item.FunctionArg
	}
	return nil
}

/******************************************************************************/
// exprString can be 5 or 7 fields

// Field name     Mandatory?   Allowed values    Allowed special characters
// ----------     ----------   --------------    --------------------------
// Seconds        No           0-59              * / , -
// Minutes        Yes          0-59              * / , -
// Hours          Yes          0-23              * / , -
// Day of month   Yes          1-31              * / , - L W
// Month          Yes          1-12 or JAN-DEC   * / , -
// Day of week    Yes          0-6 or SUN-SAT    * / , - L #
// Year           No           1970–2099         * / , -
func (c *CronSecond) AddCron(name string, exprString string, function TimerFunc, arg interface{}) error {
	expr, err := cronexpr.Parse(exprString)
	if err != nil {
		return err
	}
	now := time.Now().UTC().UnixMilli()
	item := cronSecondData{CronExpr: expr, Function: function, FunctionArg: arg}
	item.NextMilli = item.GetNext(now)
	c.timerGuard.Lock()
	c.timers[name] = &item
	c.timerGuard.Unlock()
	return nil
}

func (c *CronSecond) Remove(name string) {
	c.timerGuard.Lock()
	_, ok := c.timers[name]
	if ok {
		delete(c.timers, name)
	}
	c.timerGuard.Unlock()
}

func (c *CronSecond) RemoveAll() {
	c.timerGuard.Lock()
	c.timers = make(map[string]*cronSecondData)
	c.timerGuard.Unlock()
}

func (c *CronSecond) Run() {
	defer try.Exception(func(e interface{}) {
		log.Logger.Error(try.GetErrorStackTrace(e))
	})
	if c.started {
		return
	}
	c.started = true
	defaultSleep := time.Duration(TIME_WAIT_SEC) * time.Second
	for {
		sleepTime := defaultSleep
		select {
		case <-c.signStop:
			// 退出
			return
		case <-time.After(sleepTime):
			c.timerGuard.Lock()
			now := time.Now().UTC().UnixMilli()
			for _, t := range c.timers {
				nextMilli := t.GetNext(now)
				if t.NextMilli-now <= 0 {
					// 立即启动
					go t.Function(t.NextMilli/1000, t.FunctionArg)
				}
				t.NextMilli = nextMilli
			}
			c.timerGuard.Unlock()
		}
	}
}

func (c *CronSecond) Start() {
	go c.Run()
}

func (c *CronSecond) Stop() {
	c.signStop <- true
}
