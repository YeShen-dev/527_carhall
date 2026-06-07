# AutoParts Mall — API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8833/api`
- **认证方式**: Bearer JWT (Header: `Authorization: Bearer <token>`)
- **Content-Type**: `application/json`

---

## 一、用户认证

### 1.1 登录

```
POST /api/auth/login
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |
| clientType | string | 否 | `web`(需要验证码) / `miniapp`(跳过验证码) |
| captchaUuid | string | Web必填 | 验证码UUID |
| captchaCode | string | Web必填 | 验证码 |

**响应**: `{ code:200, data: { token, refreshToken, user: { id, username, phone, email, avatar, role } } }`

前端调用:
```js
import { login } from '@/api/auth.js'
const res = await login(username, password, captchaUuid, captchaCode, 'web')
// res.data.token / res.data.user
```

### 1.2 刷新 Token

```
POST /api/auth/refresh
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | string | 是 | 刷新令牌 |

---

## 二、用户中心

### 2.1 获取用户信息

```
GET /api/user/info
Authorization: Bearer <token>
```

**响应**: `{ code:200, data: { id, username, phone, email, avatar, role } }`

前端调用:
```js
import { getUserInfo } from '@/api/user.js'
const res = await getUserInfo()
// res.data.avatar → 头像URL
```

### 2.2 更新个人信息

```
PUT /api/user/info
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 否 | 手机号 |
| email | string | 否 | 邮箱 |
| avatar | string | 否 | 头像URL (上传后获得) |

前端调用:
```js
await request.put('/user/info', { phone: '13800138000', email: 'test@test.com' })
```

### 2.3 修改密码

```
PUT /api/user/password
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | string | 是 | 原密码 |
| newPassword | string | 是 | 新密码 (≥6位) |

前端调用:
```js
await request.put('/user/password', { oldPassword: '123456', newPassword: '654321' })
```

### 2.4 头像上传

```
POST /api/upload/avatar
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 图片 (jpg/png/gif/webp, ≤2MB) |

**响应**: `{ code:200, data: { url: "https://oss.xxx/avatars/xxx.jpg" } }`

---

## 三、商品评论

### 3.1 发表评论

```
POST /api/comment/add
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | Long | 是 | 商品ID |
| orderId | Long | 是 | 订单ID (需COMPLETED) |
| content | string | 是 | 评论内容 (10-500字) |
| rating | int | 是 | 评分 1-5 |
| images | string | 否 | 图片JSON数组 |
| isAnonymous | int | 否 | 0=实名 1=匿名 |
| isAppend | int | 否 | 0=首次 1=追评 |

前端调用:
```js
await request.post('/comment/add', {
  productId, orderId, content, rating, isAnonymous: 0, isAppend: 0
})
```

### 3.2 商品评论列表

```
GET /api/comment/product/{productId}?rating=5&page=0&size=10
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "list": [{
      "id": 1, "username": "admin", "rating": 5, "content": "...",
      "images": ["url1","url2"], "likeCount": 10, "replyCount": 2,
      "isAnonymous": 0, "isAppend": 0, "liked": false,
      "replies": [{"id":1,"username":"商家","content":"感谢评价","isMerchant":1}],
      "createTime": "2025-01-01T12:00:00"
    }],
    "total": 25, "page": 0, "size": 10
  }
}
```

### 3.3 评论详情

```
GET /api/comment/{id}
```

### 3.4 编辑评论

```
PUT /api/comment/{id}
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | string | 是 | 修改后内容 (10-500字) |
| rating | int | 是 | 修改后评分 |

### 3.5 删除评论

```
DELETE /api/comment/{id}
Authorization: Bearer <token>
```

仅作者可删除。

### 3.6 点赞/取消

```
POST /api/comment/{id}/like
Authorization: Bearer <token>
```

**响应**: `{ code:200, data: { liked: true } }` (toggle)

### 3.7 回复评论

```
POST /api/comment/reply
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Long | 是 | 评论ID |
| content | string | 是 | 回复内容 |
| parentId | Long | 否 | 回复目标ID (二级回复) |

### 3.8 评论统计

```
GET /api/comment/statistics/{productId}
```

**响应**: `{ totalCount, goodCount, middleCount, badCount, goodRate: "96%", avgRating: 4.7 }`

### 3.9 我的评论

```
GET /api/comment/my?type=0&page=0&size=10
```

type: 0=首次评论, 1=追评

### 3.10 待评论商品

```
GET /api/comment/pending
Authorization: Bearer <token>
```

返回已完成但未评论的商品列表。

---

## 四、管理后台 — 评论

### 4.1 评论列表

```
GET /api/admin/comment/list?status=0&keyword=&page=0&size=10
Authorization: Bearer <token> (需要 ADMIN 角色)
```

### 4.2 审核评论

```
POST /api/admin/comment/review
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 评论ID |
| status | int | 是 | 1=通过, 2=拒绝 |

### 4.3 删除评论

```
DELETE /api/admin/comment/{id}
Authorization: Bearer <token> (ADMIN)
```

---

## 通用错误响应

```json
{ "code": 400, "message": "错误描述", "data": null }
{ "code": 401, "message": "未登录", "data": null }
{ "code": 500, "message": "服务器内部错误", "data": null }
```

## 小程序 API 调用示例

```js
// 登录 (免验证码)
uni.request({
  url: 'http://localhost:8833/api/auth/login',
  method: 'POST',
  data: { username: 'admin', password: '123456', clientType: 'miniapp' },
  success(res) {
    const { token, user } = res.data.data
    uni.setStorageSync('token', token)
  }
})

// 带 Token 请求
uni.request({
  url: 'http://localhost:8833/api/comment/product/1',
  method: 'GET',
  header: { 'Authorization': 'Bearer ' + uni.getStorageSync('token') },
  success(res) { console.log(res.data.data.list) }
})
```
