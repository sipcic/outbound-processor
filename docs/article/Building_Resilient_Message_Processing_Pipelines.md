# 🛡️ Building Resilient Message Processing Pipelines: Understanding Restart Safety vs. Crash Tolerance

In modern data processing systems, especially those involving message queues, file transformations, and real-time pipelines, **resilience** is critical. But what exactly does "resilience" mean in practice?

In this article, we’ll explore the difference between a system that is **restart-safe** and one that is truly **crash-tolerant** — through the lens of a message-processing pipeline that transforms XML messages from a JMS queue into CSV files.

---

## ✅ Scenario: XML-to-CSV Message Processing System

Imagine a Spring Boot + Apache Camel application that:

1. Continuously consumes messages from a **JMS queue** (e.g., ActiveMQ Artemis)
2. Transforms each **XML message into a CSV row**
3. Appends it to a **working file (`working.csv`)**
4. On receiving an **EOF marker**, rotates the file to `output/output_<timestamp>.csv`

This setup leverages Camel’s `.transacted()` DSL, ensuring messages are processed within a transactional JMS context.

---

## 🔁 Restart Test Validation

To validate the system’s resilience, the following test was performed:

1. A message was sent to the JMS queue.
2. The message was processed and written to `working.csv`.
3. The application was restarted.
4. The `working.csv` file persisted on disk across the restart.
5. An EOF message was sent after the restart.
6. The system rotated the file successfully to the `output/` folder.

### ✔️ Result

The test **confirmed that the system is restart-safe** under controlled conditions:
- JMS transactions ensured messages were not lost.
- The working CSV file remained intact on disk.
- File rotation logic resumed correctly after restart.

---

## ⚠️ But Is It Crash-Tolerant?

While the system handled restarts well, it's important to distinguish between **restart safety** and **crash tolerance**.

| Scenario                                                | Restart-Safe System | Crash-Tolerant System |
|---------------------------------------------------------|---------------------|------------------------|
| Clean application restart                               | ✅ Yes              | ✅ Yes                |
| System crash during file write                          | ❓ Maybe (depends on file flush) | ✅ Yes (via logging) |
| Message consumed but not written to file before crash   | ❌ No               | ✅ Yes                |
| File deleted or corrupted externally                    | ❌ No               | ✅ Yes                |
| Resume processing from partial or unknown state         | ❌ No               | ✅ Yes                |

---

## 🧩 What's Missing for Crash Tolerance?

To make the system crash-tolerant, additional mechanisms are required:

### 1. **Persistent Message State Logging**
Each message should be stored in a durable database (e.g., H2, PostgreSQL) with a status such as:
- `RECEIVED`
- `WRITTEN`
- `ROTATED`

### 2. **Idempotent File Writes**
Before writing to the CSV file, the system should:
- Check if the message has already been processed
- Skip duplicate writes

### 3. **Transaction-Aware File Output**
The system should:
- Write to the file **after** a message is successfully logged
- Update message status to `WRITTEN` only after successful file append

### 4. **Recovery Logic on Startup**
On application startup, the system should:
- Query the DB for any `RECEIVED` messages
- Resume writing to the file or perform reconciliation if needed

---

## 🏗️ Architectural Blueprint for Crash-Tolerant Pipelines

```plaintext
[ Artemis Queue ]
       ↓
   [ Camel Route ]
       ↓
[ Save to DB → Status = RECEIVED ]
       ↓
[ Write to working.csv ]
       ↓
[ Update Status = WRITTEN ]
       ↓
[ On EOF → Rotate File ]
       ↓
[ Update Status = ROTATED ]
```

This design ensures that **no message is lost**, **no duplicate lines are written**, and the system can **recover gracefully from any failure**.

---

## ✅ Conclusion

While transactional messaging and persistent queues offer a strong foundation for data reliability, **true crash tolerance requires persistent state management outside of volatile memory and temporary files**. Your system may be restart-safe, but crash tolerance demands a higher level of rigor — and with it, operational confidence.

By combining JMS transactions with database-backed status tracking, you can build robust, fault-tolerant pipelines capable of surviving unexpected failures, crashes, or outages.
