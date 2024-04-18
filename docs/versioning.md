# How to make a new version

- If you want to create a new version you should have access to the packagecloud repository of uam

## Packagecloud configuration

To use packagecloud, you need to put the following authentication information in your local `.m2/settings.xml` (usually in your home folder, e.g. `~/.m2/settings.xml` on Linux):

```xml
<settings>
  <servers>
    <server>
     <id>packagecloud-uam</id>
     <username>USERNAME</username>
     <password>API TOKEN</password>
    </server>
  </servers>
</settings>
```

The `USERNAME` is your username on packagecloud which has access to the repository. The `API TOKEN` you can generate on packagecloud after your are logged in. The `id` of the server needs to stay as is here, as it is referenced in the `pom.xml` of eqasim.

## Which new version number to choose?

- We follow `semver`, which means `MAJOR.MINOR.PATCH`
- `PATCH` is increased if bugs got fixed and no interfaces have been changed.
- `MINOR` is updated when interfaces change, new functionality is added.
- `MAJOR` is updated when major changes have been done (whatever this means exactly :)

## Step by step

- Create a new branch `versionX.X.X` based on the current `master`
- In the new branch, update the version in the Maven packages by
  - `mvn versions:set` and then giving the new version number `X.X.X`
  - `mvn versions:commit` to accept the preliminary changes (which you can verify) in the `pom.xml`
- Update all the references to the old version `Y.Y.Y` with the new version `X.X.X` in `README.md`
- Update the `README.md`
  - ... by replacing **Development version** with **X.X.X**
  - ... by adding a new section **Development version** on top of the changelog and one bullet point "No changes yet"
- Commit the changes to the new branch
- Create a PR on Github and wait until the test have passed.
- Merge the PR on Github in case there are no errors. Call the PR "Release X.X.X".
- Locally, check out the updated `master` branch
- Run `git tag vX.X.X` to create the new verison tag
- Run `git push --tags` to push the new version tag
- Run `mvn deploy` to deploy the Maven artifacts to Bintray
