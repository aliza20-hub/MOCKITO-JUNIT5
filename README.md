                     CLIENT
                       │
                       ▼
                Spring Security
                       │
                  JWT Filter
                       │
                       ▼
               SecurityContext
                       │
                       ▼
                 Controller
                       │
                  @Valid DTO
                       │
                       ▼
                   Service
                       │
               @Transactional
                       │
                       ▼
                 Repository
                       │
                       ▼
                 Hibernate/JPA
                       │
                       ▼
                  PostgreSQL
