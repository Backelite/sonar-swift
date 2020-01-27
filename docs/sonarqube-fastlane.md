# Using with fastlane 🚀

If you already use fastlane, you can simply setup a new lane performing the analysis as follows:

Add `fastlane-plugin-lizard` and `java-properties` gems into `Gemfile`, run `bundle install`

Then copy the file `sonar-swift-plugin/src/main/fastlane/sonar.rb` in your project and import it in your Fastfile.

```ruby
import "sonar.rb"
```

The lane `:metrics` is then available. To run the analysis, call:

```bash
$ fastlane metrics
```

## `sonar-project.properties`

Please note that, in order to have your analysis performed via the tools above, you'll need to setup your `sonar-project.properties` file.