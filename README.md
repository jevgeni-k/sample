implemented within strict timeframe, possible improvements:

- better locking mechanism to guarantee data consistency when running the app on multiple nodes
- more strict code structure (ports and adapters), perform business logic on Core objects only (POJOs) (i.e. apply principles of Hex architecture)
- ArchUnit tests to verify that each layer uses appropriate classes/imports (e.g. Entity objects are not used in Controllers, Core is clean from anything too specific, etc etc)
- more and better validations
- better exception handling
- use enums for currency
- add tests
