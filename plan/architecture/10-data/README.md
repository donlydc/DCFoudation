# Data architecture

Read only the subdocuments needed for the current data path.

```text
Repository contract
   ├─ RemoteDataSource
   ├─ LocalDataSource
   └─ Cache/offline policy
```

Data implementation types must not leak casually into presentation/public contracts.
