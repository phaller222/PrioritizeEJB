# How to contribute

**IMPORTANT: Please note the projects git workflow: Pull requests and commits go to the develop branch first!**

I'm really glad you're reading this, because we need volunteer developers to help this project come to fruition.

If you haven't already seen, read the short summary of what prioritize is all about: http://www.prioritize-iot.de. 
We want you working on things you're excited about.

Here are some important resources:

  * [Prioritize website](http://www.prioritize-iot.de) tells you about the goals of the project,
  * [Jenkins buildserver](http://jenkins.prioauth.com:8080 is our build server. well known contributors get a login :-)
  
Since this project is very new we don't have any regular IRC or videoconferencing sessions. 
  
## Testing

We admit, automated tests have not been considered during development. But feel free to provide us with a useful concept. But
keep in mind that if we implement that concept, you will have the main responsibility of maintaining that.

## Submitting changes

Please send a 
GitHub Pull Request to [PrioritizeEJB](https://github.com/phaller222/PrioritizeEJB/pull/new/master) or 
                       [PrioritizeWeb](https://github.com/phaller222/PrioritizeWeb/pull/new/master) for the web part
with a clear list of what you've done (read more about [pull requests](http://help.github.com/pull-requests/)). 
When you send a pull request, we will love you forever if you include examples. Please follow our coding conventions and make sure all of your commits are atomic (one feature per commit).

Always write a clear log message for your commits. One-line messages are fine for small changes, but bigger changes should look like this:

    $ git commit -m "A brief summary of the commit
    > 
    > A paragraph describing what changed and its impact."

## Coding conventions

Start reading our code and you'll get the hang of it. Additionaly:

  * We put spaces after list items and method parameters (`[1, 2, 3]`, not `[1,2,3]`), around operators (`x += 1`, not `x+=1`), and around hash arrows.
  * This is open source software. Consider the people who will read your code, and make it look nice for them. It's sort of like driving a car: Perhaps you love doing donuts when you're alone, but with passengers the goal is to make the ride as smooth as possible.
 
Thanks,
Peter Michael Haller, initiator of Prioritize
