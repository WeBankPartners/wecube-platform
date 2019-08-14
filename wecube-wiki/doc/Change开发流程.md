1, 所有chagne必须来源于/挂钩于一个Issue;<br>
2, 由change创建Feature/hotfix的branch，命名规则 - {issueid}_{issue描述/名称}，例如 123_create_new_container; <br>
3，经过测试和Reviwer之后，再merge到Dev的branch(如果是hotfix可以直接到Master）<br>
4，所有提交的comments规范为 - [issueid] - 提交的描述。（描述应该说明为什么，而不是做了什么）<br>