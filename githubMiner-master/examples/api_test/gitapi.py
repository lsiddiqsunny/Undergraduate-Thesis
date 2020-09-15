# https://api.github.com/repos/org/repo

import urllib.parse
import requests

api = 'https://api.github.com/repos/MyCollab/mycollab'
# https://api.github.com/repos/django/django/languages

# param  = 'lhr'
# url  = api + urllib.parse.urlencode({'address':param})

json_data = requests.get(api).json()

print(json_data)

'''
## JSON dump

{
    "id": 4164482,
    "node_id": "MDEwOlJlcG9zaXRvcnk0MTY0NDgy",
    "name": "django",
    "full_name": "django/django",
    "private": false,
    "owner": 
    {
        "login": "django",
        "id": 27804,
        "node_id": "MDEyOk9yZ2FuaXphdGlvbjI3ODA0",
        "avatar_url": "https://avatars2.githubusercontent.com/u/27804?v=4",
        "gravatar_id": "",
        "url": "https://api.github.com/users/django",
        "html_url": "https://github.com/django",
        "followers_url": "https://api.github.com/users/django/followers",
        "following_url": "https://api.github.com/users/django/following{/other_user}",
        "gists_url": "https://api.github.com/users/django/gists{/gist_id}",
        "starred_url": "https://api.github.com/users/django/starred{/owner}{/repo}",
        "subscriptions_url": "https://api.github.com/users/django/subscriptions",
        "organizations_url": "https://api.github.com/users/django/orgs",
        "repos_url": "https://api.github.com/users/django/repos",
        "events_url": "https://api.github.com/users/django/events{/privacy}",
        "received_events_url": "https://api.github.com/users/django/received_events",
        "type": "Organization",
        "site_admin": false
    },
    "html_url": "https://github.com/django/django",
    "description": "The Web framework for perfectionists with deadlines.",
    "fork": false,
    "url": "https://api.github.com/repos/django/django",
    "forks_url": "https://api.github.com/repos/django/django/forks",
    "keys_url": "https://api.github.com/repos/django/django/keys{/key_id}",
    "collaborators_url": "https://api.github.com/repos/django/django/collaborators{/collaborator}",
    "teams_url": "https://api.github.com/repos/django/django/teams",
    "hooks_url": "https://api.github.com/repos/django/django/hooks",
    "issue_events_url": "https://api.github.com/repos/django/django/issues/events{/number}",
    "events_url": "https://api.github.com/repos/django/django/events",
    "assignees_url": "https://api.github.com/repos/django/django/assignees{/user}",
    "branches_url": "https://api.github.com/repos/django/django/branches{/branch}",
    "tags_url": "https://api.github.com/repos/django/django/tags",
    "blobs_url": "https://api.github.com/repos/django/django/git/blobs{/sha}",
    "git_tags_url": "https://api.github.com/repos/django/django/git/tags{/sha}",
    "git_refs_url": "https://api.github.com/repos/django/django/git/refs{/sha}",
    "trees_url": "https://api.github.com/repos/django/django/git/trees{/sha}",
    "statuses_url": "https://api.github.com/repos/django/django/statuses/{sha}",
    "languages_url": "https://api.github.com/repos/django/django/languages",
    "stargazers_url": "https://api.github.com/repos/django/django/stargazers",
    "contributors_url": "https://api.github.com/repos/django/django/contributors",
    "subscribers_url": "https://api.github.com/repos/django/django/subscribers",
    "subscription_url": "https://api.github.com/repos/django/django/subscription",
    "commits_url": "https://api.github.com/repos/django/django/commits{/sha}",
    "git_commits_url": "https://api.github.com/repos/django/django/git/commits{/sha}",
    "comments_url": "https://api.github.com/repos/django/django/comments{/number}",
    "issue_comment_url": "https://api.github.com/repos/django/django/issues/comments{/number}",
    "contents_url": "https://api.github.com/repos/django/django/contents/{+path}",
    "compare_url": "https://api.github.com/repos/django/django/compare/{base}...{head}",
    "merges_url": "https://api.github.com/repos/django/django/merges",
    "archive_url": "https://api.github.com/repos/django/django/{archive_format}{/ref}",
    "downloads_url": "https://api.github.com/repos/django/django/downloads",
    "issues_url": "https://api.github.com/repos/django/django/issues{/number}",
    "pulls_url": "https://api.github.com/repos/django/django/pulls{/number}",
    "milestones_url": "https://api.github.com/repos/django/django/milestones{/number}",
    "notifications_url": "https://api.github.com/repos/django/django/notifications{?since,all,participating}",
    "labels_url": "https://api.github.com/repos/django/django/labels{/name}",
    "releases_url": "https://api.github.com/repos/django/django/releases{/id}",
    "deployments_url": "https://api.github.com/repos/django/django/deployments",
    "created_at": "2012-04-28T02:47:18Z",
    "updated_at": "2019-02-24T19:24:30Z",
    "pushed_at": "2019-02-24T15:45:02Z",
    "git_url": "git://github.com/django/django.git",
    "ssh_url": "git@github.com:django/django.git",
    "clone_url": "https://github.com/django/django.git",
    "svn_url": "https://github.com/django/django",
    "homepage": "https://www.djangoproject.com/",
    "size": 186058,
    "stargazers_count": 39682,
    "watchers_count": 39682,
    "language": "Python",
    "has_issues": false,
    "has_projects": false,
    "has_downloads": true,
    "has_wiki": false,
    "has_pages": false,
    "forks_count": 17099,
    "mirror_url": null,
    "archived": false,
    "open_issues_count": 183,
    "license": 
    {
        "key": "other",
        "name": "Other",
        "spdx_id": "NOASSERTION",
        "url": null,
        "node_id": "MDc6TGljZW5zZTA="
    },
    "forks": 17099,
    "open_issues": 183,
    "watchers": 39682,
    "default_branch": "master",
    "organization": 
    {
        "login": "django",
        "id": 27804,
        "node_id": "MDEyOk9yZ2FuaXphdGlvbjI3ODA0",
        "avatar_url": "https://avatars2.githubusercontent.com/u/27804?v=4",
        "gravatar_id": "",
        "url": "https://api.github.com/users/django",
        "html_url": "https://github.com/django",
        "followers_url": "https://api.github.com/users/django/followers",
        "following_url": "https://api.github.com/users/django/following{/other_user}",
        "gists_url": "https://api.github.com/users/django/gists{/gist_id}",
        "starred_url": "https://api.github.com/users/django/starred{/owner}{/repo}",
        "subscriptions_url": "https://api.github.com/users/django/subscriptions",
        "organizations_url": "https://api.github.com/users/django/orgs",
        "repos_url": "https://api.github.com/users/django/repos",
        "events_url": "https://api.github.com/users/django/events{/privacy}",
        "received_events_url": "https://api.github.com/users/django/received_events",
        "type": "Organization",
        "site_admin": false
    },
    "network_count": 17099,
    "subscribers_count": 2165
}

'''