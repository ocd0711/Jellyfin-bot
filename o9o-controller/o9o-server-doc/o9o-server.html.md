# o9o-server
Version |  Update Time  | Status | Author |  Description
---|---|---|---|---
0.1|♾️|update|OCD|none



## 后台管理-用户管理
### 查询用户列表
**URL:** /userm/findUsers

**Type:** POST

**Author:** OCD

**Content-Type:** application/json

**Description:** 查询用户列表

**Query-parameters:**

Parameter | Type|Description|Required|Since
---|---|---|---|---
token|string|     后台管理 token|true|-
fuzzyQuery|string|模糊查询信息|false|-

**Body-parameters:**

Parameter | Type|Description|Required|Since
---|---|---|---|---
currentPage|int32|当前页号|false|-
pageSize|int32|每页条数|false|-

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -i /userm/findUsers?token=4e0z6q&fuzzyQuery=6htpr1 --data '{
  "currentPage": 1,
  "pageSize": 10
}'
```
**Response-fields:**

Field | Type|Description|Since
---|---|---|---
status|int32|接口查询状态|-
statusMsg|string|接口查询状态描述|-
data|object|接口查询结果|-
└─currentPage|int64|当前页|-
└─pageSize|int64|页大小|-
└─totalCount|int64|总记录数|-
└─totalPage|int64|总页数|-
└─list|array|No comments found.|-
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─id|int32|用户 id|-
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─tgId|string|tg id|-
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─embyId|string|emby id|-
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─email|string|用户邮箱|-
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─userType|int32|0:预留账户 1:启用账号 2:白名单账号 3:封禁用户|-
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─createTime|object|创建时间|-

**Response-example:**
```
{
  "status": 537,
  "statusMsg": "dvb53h",
  "data": {
    "currentPage": 923,
    "pageSize": 354,
    "totalCount": 145,
    "totalPage": 37,
    "list": [
      {
        "id": 875,
        "tgId": "9",
        "embyId": "9",
        "email": "johnetta.hirthe@gmail.com",
        "userType": 939,
        "createTime": "2022-12-14 09:50:24"
      }
    ]
  }
}
```

### 新建用户
**URL:** /userm/addUser

**Type:** POST

**Author:** OCD

**Content-Type:** application/json

**Description:** 新建用户

**Query-parameters:**

Parameter | Type|Description|Required|Since
---|---|---|---|---
token|string|  后台管理 token|true|-

**Body-parameters:**

Parameter | Type|Description|Required|Since
---|---|---|---|---
tgId|string|tg id|false|-
embyId|string|emby id|false|-
email|string|用户邮箱|false|-
userType|int32|0:预留账户 1:启用账号 2:白名单账号 3:封禁用户|false|-

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -i /userm/addUser?token=92itpl --data '{
  "tgId": "9",
  "embyId": "9",
  "email": "johnetta.hirthe@gmail.com",
  "userType": 436
}'
```
**Response-fields:**

Field | Type|Description|Since
---|---|---|---
status|int32|接口查询状态|-
statusMsg|string|接口查询状态描述|-
data|object|接口查询结果|-

**Response-example:**
```
{
  "status": 155,
  "statusMsg": "hnb77m",
  "data": {}
}
```


