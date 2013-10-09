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

##Example
    # Credits for original, kookster

    httpd_listen(80)

    bind('http_request', null, null, @event,
        modify_event('code', 200)
        httpd_set_header('Content-Type', 'text/html; charset=UTF-8')

        @playerlist = ''

        foreach(all_players(), @player){
            @playerlist = @playerlist . color('white') . @player . ', '
        }

        @playerlist = reg_replace(', $', '', @playerlist)

        @header = color('GOLD') . ' ----------------- Players Online: ' . color('yellow') . array_size(all_players()) . color('GOLD') . ' -----------------'

        @body = 
            '<html>
                <head></head>
                <body style="background-color:black;">
                    ' . @header . '
                    <br>
                    ' . @playerlist . '
                </body>
            </html>'

        @colors = array(
            'Black':        color('BLACK'),
            'DarkBlue':     color('DARK_BLUE'),
            'DarkGreen':    color('DARK_GREEN'),
            'DarkCyan ':    color('DARK_AQUA'),
            'DarkRed':      color('DARK_RED'),
            'DarkMagenta':  color('DARK_PURPLE'),
            'Gold':         color('GOLD'),
            'Gray':         color('GRAY'),
            'DimGray':      color('DARK_GRAY'),
            'Blue':         color('BLUE'),
            'Green':        color('GREEN'),
            'Cyan':         color('AQUA'),
            'Red':          color('RED'),
            'Fuchsia':      color('LIGHT_PURPLE'),
            'Yellow':       color('YELLOW'),
            'White':        color('WHITE')
        )

        foreach(array_keys(@colors), @color){
            @colorReplace	= '<span style="color:' . @color . '">'
            @body		= reg_replace(@colors[@color], @colorReplace, @body)
        }

        modify_event('body', @body)
    )