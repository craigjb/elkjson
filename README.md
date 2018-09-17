# elkjson
Simple application to use the Eclipse Layout Kernel (ELK) to perform layout on an input JSON graph and produce and output JSON file with the resulting layout.

The input and output use the [ELK JSON format](https://www.eclipse.org/elk/documentation/tooldevelopers/graphdatastructure/jsonformat.html).

## Requirements
Requires Maven for building.

On macOS: `brew install maven`

For others: <https://maven.apache.org/install.html>

## Building
`mvn clean package jar:jar appassembler:assemble`

## Running
The binary is built at `target/appassembler/bin/elkjson`

The command line args are:
```
usage: elkjson
 -i,--input <arg>             input JSON graph file path
 -j,--pretty-json             output pretty JSON
 -l,--layout-provider <arg>   layout provider class name
 -o,--output <arg>            output JSON graph file path
 -p,--layout-package <arg>    layout package
 ```

`layout-package` should be one of the sub-packages in `org.eclipse.elk.alg` (e.g. `layered`).
`layout-provider` should be the class name of the layout provider in the package (e.g. `LayeredLayoutProvider`)
