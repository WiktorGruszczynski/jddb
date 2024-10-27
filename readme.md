# Java Discord Database




## Discord Repository

| Method         | Description                                                                                 |
|----------------|---------------------------------------------------------------------------------------------|
| save           | saves entity in table                                                                       |
| saveAll        | saves array of entities in table                                                            |
| findById       | if entity with specified ID exists, returns it in other case method returns empty Optional  |
| findAll        | returns all entities                                                                        |
| existsById     | returns either true if entity with specified ID exists in database, or false if it does not |
| count          | returns number of entities                                                                  |
| deleteById     | removes entity with specified ID                                                            |
| delete         | removes passed entity from table                                                            |
| deleteAll      | removes passed array of entities, if no arguments are passed it clears entire table         |
| executeQuery   | takes Query object and returns objects based on its content                                 |



## Query syntax

```java
public List<User> getByName(){
    return executeQuery(
            new Query<User, User>()
                    .SELECT("*")
                    .WHERE("name=Adam")
                    .AND()
                    .WHERE("age>30")
    );
}
```

## Customizable column names


```java
@Column(name = "some other name")
private String surname;
```