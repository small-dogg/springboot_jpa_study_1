## OSIV와 성능 최적화
- Open Session In View: 하이버네이트
- Open EntityManager In View: JPA
- 관례상 OSIV라고 부른다.

> JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning

- `spring.jpa.open-in-view` : 기본 값

언제 JPA가 `DB Connection`을 획득하고, 언제 반환하는가에 대한 문제

DB Transaction을 시작할 때(@Transactional 이 작성된 부분으로 접근을 시작할 때) `DB Connection` 획득
Controller에서 반환을 하기 전까지 반환하지 않음.

즉, OSIV가 true인 경우는 API 가 User에게 반환이 될 때 까지, Transation이 시작하고 Controller에서 반환하고, View Resolving이 끝나는 시점까지 살아 있게 함.

이것이 바로 지연로딩이 가능했던 이유.

**치명적인 단점**
너무 오랫동안 DB Connection 리소스를 사용하기 때문에, 실시간 트래픽이 중요한 경우에는
애플리케이션에서는 커넥션이 말라버릴 수 있음.
예를 들어서 컨트롤러에서 외부 API를 호출하면 외부 API 대기 시간 만큼 커넥션 리소스를 반환 못하고 유지해야됨...

---

### OSIV OFF
트랜잭션 범위 내에서만 영속성 컨텍스트 내 트랜잭션의 작업을 유지하고, 반환되면(Controller로 반환되면) DB Connection이 반환되고,
영속성 컨텍스트도 종료됨.

때문에, 커넥션 리소스를 낭비하지 않음

대신, 지연로딩을 트랜잭션 안에서 처리해야함. 또한, view template에서는 지연로딩이 동작하지 않음.

결론은 트랜잭션 범위 내에서 지연 로딩을 강제로 모두 수행해두어야 함.

**LazyInitializationException** 

---

### 