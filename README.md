# Text2SQL: Natural Language to SQL with Spring Boot, Hugging Face, and pgvector

This project enables converting natural language questions into executable SQL queries using a combination of semantic embeddings, vector search, and prompt-based generation. Built with **Spring Boot** and integrated with **Hugging Face APIs** and **PostgreSQL (pgvector)**, it provides a scalable backend for text-to-SQL translation.

---

## Features

- 💬 Accepts user input in plain English and returns relevant SQL queries  
- 🧠 Uses Hugging Face for embedding generation and query generation  
- 📊 Performs vector similarity search over schema and metadata stored in pgvector  
- 🔍 Dynamically builds context from metadata to guide query generation  
- ⚠️ Returns fallback response when schema context is insufficient for query formation

---

## Tech Stack

- **Backend**: Java + Spring Boot  
- **Embeddings & Chat**: Hugging Face APIs  
- **Database**: PostgreSQL with `pgvector` extension  
- **Search Method**: Hybrid (semantic + metadata relevance)  
- **API Style**: REST

---
