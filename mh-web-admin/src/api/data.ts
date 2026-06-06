import { http } from "@/utils/http";
import type { Result } from "@/api/system";
import { getToken } from "@/utils/auth";
/** 获取用户数量（支持按用户类型筛选） */
export const getAllUsersCount = (userType?: string | number) => {
  const userData = getToken();
  return http.request<Result>("get", "/admin/count/getAllUsersCount", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    params: { user_type: userType }
  });
};

/** 获取菌种卡片数量 */
export const getAllMushroomsCount = () => {
  const userData = getToken();
  return http.request<Result>("get", "/admin/count/getAllMushroomsCount", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    }
  });
};

/** 获取科普内容数量（支持按菌种Id/内容类型筛选） */
export const getAllPopSciContentsCount = (mushroomId?: number, contentType?: string) => {
  const userData = getToken();
  return http.request<Result>("get", "/admin/count/getAllPopSciContentsCount", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData?.accessToken || ""
    },
    params: {
      mushroomId, // 严格匹配后端的参数名：mushroomId（Long类型）
      contentType
    }
  });
};

/** 获取病虫害数量 */
export const getAllDiseasePestsCount = () => {
  const userData = getToken();
  return http.request<Result>("get", "/admin/count/getAllDiseasePestsCount", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    }
  });
};
// /** 获取用户数量 */
// export const getAllUsersCount = () => {
//   const userData = getToken();
//   return http.request<Result>("get", "/admin/user/getAllUsersCount", {
//     headers: {
//       "Content-Type": "application/json",
//       Authorization: userData.accessToken
//     }
//   });
// };

// /** 获取歌手数量 */
// export const getAllArtistsCount = (gender?: number, area?: string) => {
//   const userData = getToken();
//   return http.request<Result>("get", "/admin/getAllArtistsCount", {
//     headers: {
//       "Content-Type": "application/json",
//       Authorization: userData.accessToken
//     },
//     params: { gender, area }
//   });
// };

// /** 获取歌曲数量 */
// export const getAllSongsCount = (style?: string) => {
//   const userData = getToken();
//   return http.request<Result>("get", "/admin/getAllSongsCount", {
//     headers: {
//       "Content-Type": "application/json",
//       Authorization: userData.accessToken
//     },
//     params: { style }
//   });
// };

// /** 获取歌单数量 */
// export const getAllPlaylistsCount = (style?: string) => {
//   const userData = getToken();
//   return http.request<Result>("get", "/admin/getAllPlaylistsCount", {
//     headers: {
//       "Content-Type": "application/json",
//       Authorization: userData.accessToken
//     },
//     params: { style }
//   });
// };
