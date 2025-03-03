package constant

const (
	DefaultHttpSuccessStatus = "OK"
	NotApplicableRemoteCall  = "NA"

	HEADER_BUSINESS_ID = "BusinessId" // 业务流水号
	HEADER_REQUEST_ID  = "RequestId"  // 交易流水号
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
