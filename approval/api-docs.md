Below is a **fully expanded, OpenAPI-backed Technical API Documentation in Markdown**, with **examples for every request and response**, written in **open-source–quality style**.

You can **copy this entire response into a file named**
👉 **`docs/approval-framework-api.md`**
and commit it directly.
It is intentionally verbose, explicit, and tester/developer friendly.

---

# 📘 In-App Approval Framework

**Technical API Documentation (with Examples)**

> A lightweight, configuration-driven approval framework for Spring Boot applications.
> Supports runtime approvers, multi-level workflows, SLA enforcement, and UI-friendly APIs.

---

## 📑 Table of Contents

1. Introduction
2. Terminology
3. Authentication & Headers
4. Common Conventions
5. Approval Template APIs
6. Approval Integration APIs
7. Approval Trigger APIs
8. Approval Action APIs
9. Query APIs (Pending / Timeline)
10. Error Handling (RFC 7807)
11. End-to-End Example Flow

---

## 1️⃣ Introduction

This framework allows any business entity (Gatepass, Expense, PO, etc.) to be protected by an approval workflow **without embedding approval logic inside business code**.

The framework is:

* Configuration-driven
* Runtime-approver capable
* Multi-tenant safe
* UI-friendly (DTO-only responses)
* Event-driven and extensible

---

## 2️⃣ Terminology

| Term                 | Meaning                                            |
| -------------------- | -------------------------------------------------- |
| Approval Template    | Defines approval structure (levels, SLA, strategy) |
| Approval Integration | Attaches approval to an entity type                |
| Approval Instance    | A single execution of approval                     |
| Approval Task        | One approver’s actionable item                     |
| Runtime Approver     | Approver supplied dynamically at trigger time      |

---

## 3️⃣ Authentication & Required Headers

All APIs require:

```
Authorization: Bearer <JWT>
X-Tenant-Id: <TENANT_CODE>
Content-Type: application/json
```

---

## 4️⃣ Common Conventions

* All timestamps are **UTC ISO-8601**
* Enums are returned as **UPPERCASE strings**
* Entities are **never** returned directly
* Errors follow **RFC-7807**

---

# 5️⃣ Approval Template APIs

---

## 🔹 Create Approval Template

### Endpoint

```
POST /api/approval/templates
```

### Request Example

```json
{
  "name": "Gatepass Approval Template",
  "active": true,
  "levels": [
    {
      "levelOrder": 1,
      "assignmentStrategy": "RUNTIME_MULTI",
      "requireAllApprovals": false,
      "slaDuration": "PT24H",
      "reminderBefore": "PT4H"
    }
  ]
}
```

### Response Example (201)

```json
{
  "id": "d8ee3094-ce7b-4879-9374-0d9c645c7c53",
  "name": "Gatepass Approval Template"
}
```

---

# 6️⃣ Approval Integration APIs

---

## 🔹 Attach Template to Entity

### Endpoint

```
POST /api/approval/integrations
```

### Request Example

```json
{
  "entityType": "GATEPASS",
  "approvalTemplateId": "d8ee3094-ce7b-4879-9374-0d9c645c7c53",
  "enabled": true,
  "requireApproverInput": true
}
```

### Response Example

```json
{
  "entityType": "GATEPASS",
  "enabled": true
}
```

---

# 7️⃣ Approval Trigger APIs

---

## 🔹 Trigger Approval with Runtime Approvers

### Endpoint

```
POST /api/approvals/trigger
```

### Request Example

```json
{
  "entityType": "GATEPASS",
  "entityId": "d8b67d29-efaa-41a9-8af6-4f8d0ad66670",
  "runtimeApprovers": [
    {
      "levelOrder": 1,
      "users": ["gyan.patel", "rahul.verma"]
    }
  ]
}
```

### Response Example

```json
{
  "instanceId": "6be8fa6a-7342-425a-b2ac-0299be12deb7",
  "status": "PENDING",
  "locked": true
}
```

---

# 8️⃣ Approval Action APIs

---

## 🔹 Approve / Reject an Approval Task

### Endpoint

```
POST /api/approvals/{instanceId}/act
```

### Request (Approve)

```json
{
  "approve": true,
  "comment": "Looks good"
}
```

### Request (Reject)

```json
{
  "approve": false,
  "comment": "Incorrect information"
}
```

---

### Response Example (Approved)

```json
{
  "instanceId": "6be8fa6a-7342-425a-b2ac-0299be12deb7",
  "entityType": "GATEPASS",
  "entityId": "d8b67d29-efaa-41a9-8af6-4f8d0ad66670",
  "status": "APPROVED",
  "locked": false,
  "timeline": [
    {
      "levelOrder": 1,
      "approvalGroup": 1,
      "approverType": "USER",
      "approver": "gyan.patel",
      "status": "APPROVED",
      "actedAt": "2026-01-05T17:15:30Z",
      "actedBy": "gyan.patel",
      "comment": "Looks good"
    }
  ]
}
```

---

# 9️⃣ Query APIs

---

## 🔹 Get My Pending Approvals

### Endpoint

```
GET /api/approvals/my-pending
```

### Response Example

```json
[
  {
    "instanceId": "6be8fa6a-7342-425a-b2ac-0299be12deb7",
    "entityType": "GATEPASS",
    "entityId": "d8b67d29-efaa-41a9-8af6-4f8d0ad66670",
    "title": "Level 1 Approval",
    "actions": ["APPROVE", "REJECT"]
  }
]
```

---

## 🔹 Get Approval Timeline

### Endpoint

```
GET /api/approvals/{instanceId}/timeline
```

### Response Example

```json
[
  {
    "levelOrder": 1,
    "approvalGroup": 1,
    "approver": "gyan.patel",
    "approverType": "USER",
    "status": "APPROVED",
    "actedAt": "2026-01-05T17:15:30Z",
    "actedBy": "gyan.patel",
    "comment": "Looks good"
  }
]
```

---

# 🔟 Error Handling (RFC-7807)

---

## 🔹 Unauthorized Approval

### Response (403)

```json
{
  "type": "approval-unauthorized",
  "title": "Not authorized",
  "status": 403,
  "detail": "No pending approval task assigned to user",
  "instance": "/api/approvals/6be8fa6a-7342-425a-b2ac-0299be12deb7/act"
}
```

---

## 🔹 Approval Already Completed

### Response (409)

```json
{
  "type": "approval-already-completed",
  "title": "Approval already completed",
  "status": 409,
  "detail": "This approval instance is already APPROVED"
}
```

---

## 🔹 Concurrency Conflict

```json
{
  "type": "approval-concurrency",
  "title": "Concurrent modification",
  "status": 409,
  "detail": "Approval already processed by another user"
}
```

---

# 1️⃣1️⃣ End-to-End Example Flow

1. Create approval template
2. Attach template to entity type
3. Create entity (Gatepass)
4. Trigger approval with runtime approvers
5. Approver fetches pending approvals
6. Approver approves / rejects
7. Entity unlocks automatically
8. Timeline available for audit

---

## 📄 License

**Apache 2.0**

