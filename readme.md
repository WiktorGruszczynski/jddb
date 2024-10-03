# Java Discord Database




## Discord Repository

| Method       | Description                                                                                       |
|--------------|---------------------------------------------------------------------------------------------------|
| `save`       | saves entity in table                                                                             |
| `saveAll`    | saves array of entities in table                                                                  |
| `findById`   | if entity with specified ID exists, returns it in other case method returns empty Optional        |
| `findAll`    | returns all entities                                                                              |
| `existsById` | returns either true if entity with specified ID exists in database, or false if it does not       |
| `count`      | returns number of entities                                                                        |
| `deleteById` | removes entity with specified ID                                                                  |
| `delete `    | removes passed entity from table                                                                  |
| `deleteAll`  | function that removes passed array of entities, if no arguments are passed it clears entire table |


