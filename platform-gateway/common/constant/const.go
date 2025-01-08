package constant

const (
	DefaultHttpSuccessStatus = "OK"
	NotApplicableRemoteCall  = "NA"
)

var HttpMethods = [8]string{"GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE"}

type ServiceName string

const (
	PlatformCore       ServiceName = "platform"
	PlatformAuthServer ServiceName = "auth"
	TaskManPlugin      ServiceName = "taskman"
	MonitorPlugin      ServiceName = "monitor"
	CmdbPlugin         ServiceName = "wecmdb"
	ArtifactsPlugin    ServiceName = "artifacts"
	AdaptorPlugin      ServiceName = "adaptor"
	ItsdangerousPlugin ServiceName = "itsdangerous"
	SaltstackPlugin    ServiceName = "saltstack"
	TerminalPlugin     ServiceName = "terminal"
)
