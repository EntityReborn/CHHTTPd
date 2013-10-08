##Event:

`http_request`

@event data:

    headers: (assoc. array)
        name: (array)
            value, 
            value, 
            ...

    cookies: (assoc. array)
        name: (assoc. array)
            expires: int
            value: string
            domain: string
            path: string
            httponly: boolean

    postdata: (assoc. array)
        name: value

    host: string including port if given

    method: GET or POST

    path: string including filename.

    parameters: (assoc. array)
        key: (array)
            value, 
            value, 
            ...

`modify_event` arguments:

    body: (string) APPEND data to the body. Can be called multiple times.

    code: (int) return code (200, etc)

    contenttype: (string) type of content (text/plain, etc)

##Functions:

`httpd_listen(int)` - listen on a given port.

`httpd_unlisten(int)` - stop listening on a given port.

`httpd_set_header(key, value)` - add a header to the response.

`httpd_set_cookie(key, value)` - add a simple cookie to the response.

`httpd_set_cookie(array)` - add a defined cookie to the response. 
`array` expects at least `name` and `value`, and can contain `expires` (int), 
`domain` (string), `path` (string), and `httponly` (boolean).