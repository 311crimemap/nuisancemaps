# Typescript Notes

* Types typically follow after the variable, using a `:`.
* primitives are lowercase

`test: string | number`.

#### `interface`

* `interface` definitions use semi-colons `;` (vs an object with `,`).
* Can "union" types `|`
* Can set rigid default values (e.g. hard-coded strings or numbers)
* Optional type via `?` (like optional chaining)
* Generics like java `SetState<MyDataType>`
* Names and types can collide, so plan separate enums/types/classes
* Leverage pre-defined types in external libs

```
interface myInterface {
  myProperty: type;
}
```

##### Tuples vs Array

* Tuples are "static" in the sense the shape and size are defined.
* Arrays are typical declaration, with no fixed length

```
interface Test {
  myTuple: [number, number]
  myArray: MyInterface[] | number[] | string[]
}
```

#### Dict / Map

* "Index Signature"
  * Need to type the key since its dynamic (and type its value), but want to
    enforce key type.
* Contrast with typical objects which are really just a 'static' key

`
myMap {
    [key: string]: MyType
}
`

#### Function Types:

function: type parameters and return type:

```
function add(a: number, b: number): number {
  return a + b;
}
```

Anonymous function: same; type parameters and return type:

```
const multiply = (a: number, b: number): number => {
  return a * b;
};
```


Function type declaration:

```
let divide: (a: number, b: number) => number;  // "direct type" function; one-off

type myMethodName = (action: { type: string; value: any }) => void; // type alias for reuse
```


Generics

Example identify function; parameter `T`, returns `T`. Using `<T>` indicates a
consistent placeholder for the type throughout the definition. In contrast, just
`identity(arg: T):T` indicates a type `T` exists somewhere.


```
function identity<T>(arg: T): T {
  return arg;
}

// explicit type <number>, <string>

const result = identity<string>("Hello");  // "Hello"
const result2 = identity<number>(123); // 123
const arrayResult = identity<number[]>([1, 2, 3]);  // [1,2,3]


// inferred type

const result = identity("Hello");  // "Hello"
```


##### Type Assertion

A compile-time check; doesn't actually modify or "narrow" anything. No runtime
impact. It's really a form of consistency bypass.

`const map = MapLibre as MyCustomMap;`

### React TS

* Declare a props interface in the same file
* React has its own types, have to dig them out
  * setMyType -> `Dispatch<SetStateAction<MyType>>`


```
const[map, setMap] = useState<Map>();


# MyComponent.ts

interface myComponentProps {
    id: number,
    map: Map,
    setMap: Dispatch<SetStateAction<Map>>
}

export function MyComponent({id, map, setMap, ... (etc)}: myComponentProps)
```

