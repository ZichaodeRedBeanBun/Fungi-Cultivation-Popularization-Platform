package cn.edu.seig.MhWeb.constant;
// 允许未登录用户访问这些路径，登录拦截器中
public class PathConstant {

    public static final String PLAYLIST_DETAIL_PATH = "/playlist/getPlaylistDetail/**";
    public static final String ARTIST_DETAIL_PATH = "/artist/getArtistDetail/**";
    public static final String SONG_LIST_PATH = "/song/getAllSongs";
    public static final String SONG_DETAIL_PATH = "/song/getSongDetail/**";

}
