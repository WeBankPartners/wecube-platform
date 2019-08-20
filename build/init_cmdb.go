/*
go build -ldflags "-linkmode external -extldflags -static -s"  init_cmdb.go
*/
package main

import (
        "bytes"
        "errors"
        "fmt"
        "io/ioutil"
        "net/http"
        "os"
        "time"
)

func tryInitCmdb() error {
        cmdbUrl := os.Getenv("CMDB_SERVER_URL")
        if cmdbUrl == "" {
                return errors.New("env(CMDB_SERVER_URL) is empty")
        }

        applyAllEndpint := cmdbUrl + "/api/v2/ciTypes/applyAll"
        jsonData := []byte("{}")

        req, err := http.NewRequest("POST", applyAllEndpint, bytes.NewBuffer(jsonData))
        req.Header.Set("Content-Type", "application/json")

        client := &http.Client{}
        resp, err := client.Do(req)
        if err != nil {
                return err
        }
        defer resp.Body.Close()
        if resp.StatusCode != 200 {
                return fmt.Errorf("statusCode=%v", resp.StatusCode)
        }

        body, _ := ioutil.ReadAll(resp.Body)
        fmt.Println("response Body:", string(body))
        return nil
}

func main() {
        for i := 0; i < 10; i++ {
                if err := tryInitCmdb(); err != nil {
                        fmt.Printf("InitCmdb meet err=%v\n", err)
                        time.Sleep(time.Second * time.Duration(15))
                        continue
                }
                return
        }
}
