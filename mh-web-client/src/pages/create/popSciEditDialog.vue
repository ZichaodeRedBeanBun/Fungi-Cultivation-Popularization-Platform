<template>
  <el-dialog
    v-model="localVisible"
    width="85%"
    top="auto"
    destroy-on-close
    @close="handleClose"
    class="pop-sci-edit-dialog"
    :show-close="false"
  >
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated style="width: 100%" />
    </div>

    <div v-else class="detail-container">
      <!-- ========== 左侧媒体区 ========== -->
      <div class="content-box flex-shrink-0 bg-gray-100 overflow-hidden p-4">
        <!-- 视频类型 -->
        <div v-if="!isArticleType" class="video-content h-full flex flex-col">
          <div class="video-container relative bg-black flex-shrink-0 rounded-lg overflow-hidden" style="height: 68%;">
            <div v-if="videoCover" class="video-cover-wrapper">
              <el-image
                :src="videoCover"
                fit="cover"
                class="video-cover w-full h-full"
              >
                <template #error>
                  <div class="error-container flex items-center justify-center">
                    <img :src="defaultVideoAvatar" style="width: 100%; height: 100%; object-fit: cover;" />
                  </div>
                </template>
              </el-image>
            </div>
            <div v-else class="no-video-cover-area h-full flex items-center justify-center">
              <el-upload
                class="video-cover-uploader"
                action="#"
                :show-file-list="false"
                :http-request="(options) => handleVideoCoverUpload(null, options.file)"
                accept="image/*"
              >
                <div class="upload-btn flex flex-col items-center justify-center p-8 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary transition-colors">
                  <Icon icon="mdi:image-plus" width="48px" height="48px" class="text-gray-400" />
                  <div class="upload-text mt-4 text-lg text-gray-500">点击上传视频封面</div>
                  <div class="upload-hint mt-2 text-sm text-gray-400">支持 JPG、PNG 格式，单张不超过 2MB</div>
                </div>
              </el-upload>
            </div>

            <div v-if="videoCover" class="video-upload-btn absolute bottom-4 right-4">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :http-request="(options) => handleVideoCoverUpload(null, options.file)"
                accept="image/*"
              >
                <el-button size="small" type="primary">更换封面</el-button>
              </el-upload>
            </div>
          </div>

          <!-- 修改: 视频文件上传区域改为占满下半部分的上传区域 -->
          <div class="video-file-upload mt-4 flex-1">
            <div v-if="videoFileList.length === 0" class="video-upload-area h-full flex flex-col items-center justify-center p-8 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary transition-colors">
              <el-upload
                class="video-uploader-full"
                action="#"
                :show-file-list="false"
                :http-request="(options) => handleVideoUploadSuccess(null, options.file)"
                accept="video/*"
                :disabled="videoFileList.length > 0"
              >
                <div class="upload-icon mb-6">
                  <Icon icon="mdi:video-plus" width="48px" height="48px" class="text-gray-400" />
                </div>
                <div class="upload-text text-lg text-gray-500 mb-1">点击上传视频</div>
                <div class="upload-hint text-sm text-gray-400">仅支持单个视频文件，大小不超过100MB</div>
              </el-upload>
            </div>
            <div v-else class="video-uploaded-area h-full flex flex-col items-center justify-center p-8 border-2 border-solid border-gray-200 rounded-lg bg-gray-50 relative">
              <div class="reupload-icon mb-6">
                <Icon icon="mdi:video" width="48px" height="48px" class="text-gray-500" />
              </div>
              <div class="reupload-text text-lg text-gray-700 mb-1">已上传视频：{{ videoFileList[0].name }}</div>
              <el-upload
                class="video-reupload"
                action="#"
                :show-file-list="false"
                :http-request="(options) => handleVideoUploadSuccess(null, options.file)"
                accept="video/*"
              >
                <el-button size="small" type="primary" class="absolute bottom-4 right-4">重新上传视频</el-button>
              </el-upload>
            </div>
          </div>
        </div>

        <!-- ========== 图文类型 ========== -->
        <div v-else class="article-content h-full flex flex-col">
          <template v-if="imageList.length > 0">
            <!-- ✅ 轮播图区域：修复布局，消除底部留白 -->
            <div class="carousel-wrapper flex-shrink-0 flex items-center justify-center">
              <el-carousel
                class="article-carousel rounded-lg overflow-hidden"
                :interval="4000"
                style="width: 100%; height: 100%; background: #000;"
                :key="carouselKey"
                v-model:activeIndex="activeCarouselIndex"
              >
                <el-carousel-item v-for="(img, index) in imageList" :key="index">
                  <div class="carousel-item-wrapper relative h-full">
                    <el-image
                      :src="img.url"
                      :key="`${img.id}-${img.url}-${imageRetryMap[img.id] || 0}`"
                      fit="cover"
                      class="carousel-img w-full h-full"
                      :preview-src-list="imageList.map(i => i.url)"
                      :initial-index="index"
                      @error="(e) => handleImageError(e, img)"
                    >
                      <template #placeholder>
                        <div class="img-placeholder flex items-center justify-center">图片加载中...</div>
                      </template>
                      <template #error>
                        <div class="img-error flex items-center justify-center">
                          <div>图片加载失败</div>
                          <el-button 
                            type="text" 
                            size="small" 
                            @click.stop="() => retryImageLoad(img)"
                            class="ml-2"
                          >
                            重试
                          </el-button>
                        </div>
                      </template>
                    </el-image>
                  </div>
                </el-carousel-item>
              </el-carousel>
            </div>

            <!-- ✅ 图片列表区域：间距缩到最小 -->
            <div class="image-list-container flex-shrink-0 mt-1 overflow-x-auto">
              <div class="image-list flex gap-3 items-center py-1">
                <!-- 缩略图列表 -->
                <div
                  v-for="(img, index) in imageList"
                  :key="img.id || index"
                  class="image-list-item relative cursor-pointer rounded-lg overflow-hidden border-2 transition-all flex-shrink-0"
                  :class="{ 'border-primary': activeCarouselIndex === index, 'border-gray-200': activeCarouselIndex !== index }"
                  @click="previewImage(index)"
                >
                  <el-image
                    :src="img.url"
                    :key="`${img.id}-${img.url}-${imageRetryMap[img.id] || 0}`"
                    fit="cover"
                    class="w-24 h-24"
                    @error="(e) => handleImageError(e, img)"
                  />
                  <!-- 缩略图删除按钮 -->
                  <div class="thumbnail-delete-btn absolute -top-2 -right-2 w-6 h-6 bg-gray-400 rounded-full flex items-center justify-center cursor-pointer z-10 shadow-md" @click.stop="deleteImage(index)">
                    <Close style="width: 0.9em; height: 0.9em; color: #fff;" />
                  </div>
                </div>

                <!-- 上传按钮：紧贴最后一张图右边 -->
                <div class="upload-list-item flex-shrink-0">
                  <el-upload
                    class="image-uploader"
                    action="#"
                    :show-file-list="false"
                    :http-request="(options) => handleImageUploadSuccess(null, options.file)"
                    :before-upload="beforeImageUpload"
                    accept="image/*"
                  >
                    <div class="upload-btn-inline w-24 h-24 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary transition-colors flex items-center justify-center bg-gray-50">
                      <Icon icon="mdi:plus" width="28px" height="28px" class="text-gray-400" />
                    </div>
                  </el-upload>
                </div>
              </div>
            </div>
          </template>

          <!-- 无图片时 -->
          <div v-else class="no-image-area h-full flex items-center justify-center">
            <el-upload
              class="image-uploader"
              action="#"
              :show-file-list="false"
              :http-request="(options) => handleImageUploadSuccess(null, options.file)"
              :before-upload="beforeImageUpload"
              accept="image/*"
            >
              <div class="upload-btn flex flex-col items-center justify-center p-8 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary transition-colors">
                <Icon icon="mdi:image-plus" width="48px" height="48px" class="text-gray-400" />
                <div class="upload-text mt-6 text-lg text-gray-500">点击上传图片</div>
                <div class="upload-hint mt-2 text-sm text-gray-400">支持 JPG、PNG 格式，单张不超过 2MB</div>
              </div>
            </el-upload>
          </div>
        </div>
      </div>

      <!-- 右侧编辑区 -->
      <div class="edit-box flex-1 flex flex-col overflow-hidden bg-white">
        <div class="edit-section title-section p-6 border-b flex-shrink-0">
          <div class="flex items-center justify-between">
            <label class="edit-label text-lg font-semibold block mb-2">标题</label>
            <!-- 新增: 仅在新增模式下显示内容类型切换 -->
            <div v-if="!isEditMode" class="content-type-toggle ml-4">
              <el-segmented 
                v-model="localContentType" 
                :options="contentTypeOptions" 
                size="small"
              />
            </div>
          </div>
          <el-input
            v-model="formData.title"
            placeholder="请输入笔记标题"
            class="title-input mt-2"
            maxlength="100"
            show-word-limit
          />
        </div>

        <!-- 修改: 调整菌种标签和内容描述的布局比例 -->
        <div class="edit-section tag-description-section p-6 border-b flex-shrink-0">
          <div class="flex items-start gap-6">
            <!-- 菌种标签 - 减小宽度 -->
            <div class="basis-1/4 min-w-0">
              <label class="edit-label text-lg font-semibold block mb-2">菌种标签</label>
              <el-select
                v-model="formData.mushroomId"
                placeholder="请选择菌种标签" 
                class="w-full mt-2" 
                :loading="mushroomLoading"
                clearable
              >
                <el-option
                  v-for="item in mushroomList"
                  :key="item.id"
                  :label="item.mushroomName"
                  :value="item.id"
                />
              </el-select>

              <div class="selected-tag mt-3" v-if="formData.mushroomId !== null && formData.mushroomId !== undefined && formData.mushroomId !== 0">
                <el-tag closable @close="formData.mushroomId = null">
                  {{ mushroomList.find(item => item.id === formData.mushroomId)?.mushroomName }}
                </el-tag>
              </div>
            </div>

            <!-- 内容描述 - 增加宽度 -->
            <div class="basis-3/4 min-w-0">
              <label class="edit-label text-lg font-semibold block mb-2">内容描述</label>
              <el-input
                v-model="formData.description"
                type="textarea"
                placeholder="请输入内容描述"
                class="mt-2"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </div>
          </div>
        </div>

        <!-- 修改: 只在图文模式下显示正文内容编辑区 -->
        <div v-if="isArticleType" class="edit-section content-section p-6 flex-1 overflow-auto">
          <label class="edit-label text-lg font-semibold">正文内容</label>
          <div
            ref="contentEditorRef"
            class="content-editor"
            contenteditable="true"
            @input="handleContentEdit"
            @blur="handleContentBlur"
            placeholder="请输入正文内容..."
          ></div>
        </div>
        
        <!-- 新增: 在视频模式下添加占位区域以确保footer在底部 -->
        <div v-else class="flex-1"></div>

        <div class="edit-footer p-6 border-t flex justify-end gap-3 flex-shrink-0">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交发布</el-button>
        </div>
      </div>
    </div>

    <div class="close-btn" @click="handleClose">
      <el-button circle class="bg-white shadow-lg hover:bg-gray-50">
        <Close style="width: 1.2em; height: 1.2em; color: #666" />
      </el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import { Icon } from '@iconify/vue'
import defaultVideoAvatar from '@/assets/default.jpg'
import {
  addPopSciContent,
  updatePopSciContent,
  getMushroomSummaryPage,
  deletePicture
} from '@/api/system'
import { MushroomPageQueryDTO, MushroomSummaryVO, PictureDTO, Picture } from '@/api/interface';

// ========== Props 定义 ==========
const props = defineProps<{
  modelValue: boolean;
  editData: any;
  contentType: 'article' | 'video';
  authorId: number | string; // 修改：允许字符串类型
}>()

const emit = defineEmits(['update:modelValue', 'saveSuccess'])
const contentTypeOptions = [
  { label: '图文', value: 'article' },
  { label: '视频', value: 'video' }
]
// ========== 响应式数据 ==========
const localVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})
const loading = ref(false)
const localContentType = ref<'article' | 'video'>(props.contentType)
// 修改: 使用 localContentType 来判断类型
const isArticleType = computed(() => localContentType.value === 'article')
// ✅ 新增：用于区分新增/编辑模式
const isEditMode = computed(() => !!formData.value.id || !!props.editData?.id)

// Replace the original imageList declaration with this:
const imageList = ref<ImageItem[]>([])
// 新增：暂存区 - 图文详情图文件
const stagedImageFiles = ref<File[]>([])
const activeCarouselIndex = ref(0)
const carouselKey = ref(0)
const imageUploading = ref(false)
// 新增：用于跟踪每个图片的重试次数
const imageRetryMap = ref<Record<number, number>>({})

const videoCover = ref('')
// 新增：暂存区 - 视频封面文件
const stagedVideoCoverFile = ref<File | null>(null)
const videoCoverId = ref(0)
const videoFileList = ref<any[]>([])
// 新增：暂存区 - 视频文件
const stagedVideoFile = ref<File | null>(null)

const mushroomList = ref<{ id: number; mushroomName: string }[]>([])
const mushroomLoading = ref(false)
// Define formData type explicitly
interface FormData {
  id: number
  title: string
  mushroomId: number | null
  mushroomName: string
  content: string
  description: string
  videoUrl: string
  videoCoverUrl: string
  contentType: 'article' | 'video'
  authorId: number
  imageList?: ImageItem[]
}
const formData = ref<FormData>({
  id: 0,
  title: '',
  mushroomId: null,
  mushroomName: '',
  content: '',
  description: '',
  videoUrl: '',
  videoCoverUrl: '',
  contentType: 'article' as 'article' | 'video',
  authorId: Number(props.authorId) // 修改：转换为数字
})
    const currentFormat = ref<'p' | 'h1' | 'h2' | 'strong' | 'em'>('p')
        // Define a more flexible image item type
    interface ImageItem {
      id: number | string
      url: string
      picUrl?: string
      isTemporary?: boolean
      uploadError?: boolean
      errorMessage?: string
    }
    
    // Update the ref declaration
// ========== 核心函数定义 ==========
const initEditData = () => {
  if (props.editData) {
    // Determine the actual content type from edit data
    const actualContentType = props.editData.contentType || props.contentType;
    
    formData.value = {
      id: props.editData.id || 0,
      title: props.editData.theme || '',
      mushroomId: props.editData.mushroomId || null,
      mushroomName: props.editData.mushroomName || '',
      description: props.editData.description || '',
      content: props.editData.content || props.editData.description || '',
      contentType: actualContentType,
      authorId: Number(props.authorId),
      imageList: [],
      videoUrl: props.editData.videoUrl || '',
      videoCoverUrl: props.editData.videoCoverUrl || ''
    }

    if (props.editData.pictureList && props.editData.pictureList.length > 0) {
      const mappedImageList = props.editData.pictureList.map((pic: any) => ({
        id: pic.id,
        url: pic.picUrl || pic.url,
        picUrl: pic.picUrl || pic.url
      }))
      
      imageList.value = mappedImageList
      formData.value.imageList = mappedImageList
    }

    if (actualContentType !== 'article') {
      // 确保视频封面URL正确设置
      videoCover.value = props.editData.videoCoverUrl || ''
      videoCoverId.value = props.editData.videoCoverId || 0
      if (props.editData.videoUrl) {
        videoFileList.value = [{ name: props.editData.theme || '已上传视频', url: props.editData.videoUrl }]
      }
    }
    // 编辑模式下设置 localContentType based on actual content type
    localContentType.value = actualContentType
    
    // 确保在蘑菇列表加载完成后设置选中的蘑菇ID
    if (mushroomList.value.length > 0) {
      // 如果有mushroomId直接使用，否则通过mushroomName查找
      if (props.editData.mushroomId) {
        formData.value.mushroomId = props.editData.mushroomId || null
      } else if (props.editData.mushroomName) {
        const matchedMushroom = mushroomList.value.find(item => item.mushroomName === props.editData.mushroomName)
        formData.value.mushroomId = matchedMushroom ? matchedMushroom.id : null
      }
    } else {
      // 如果列表还未加载，等待加载完成后再设置
      loadMushroomList().then(() => {
        if (props.editData.mushroomId) {
          formData.value.mushroomId = props.editData.mushroomId || null
        } else if (props.editData.mushroomName) {
          const matchedMushroom = mushroomList.value.find(item => item.mushroomName === props.editData.mushroomName)
          formData.value.mushroomId = matchedMushroom ? matchedMushroom.id : null
        }
      })
    }
  } else {
    // 新增模式：重置表单
    formData.value = {
      id: 0,
      title: '',
      mushroomId: null,
      mushroomName: '',
      content: '',
      description: '',
      contentType: localContentType.value,
      authorId: Number(props.authorId) || 0,
      imageList: [],
      videoUrl: '',
      videoCoverUrl: ''
    }
    imageList.value = []
    videoCover.value = ''
    videoFileList.value = []
    // 新增模式下重置为默认值
    localContentType.value = 'article'
  }
}
const handleContentBlur = (e: Event) => {
  const target = e.target as HTMLElement
  formData.value.content = target.innerHTML
}
const loadMushroomList = async () => {
  try {
    const params: MushroomPageQueryDTO = { pageNum: 1, pageSize: 999 }
    mushroomLoading.value = true
    const res = await getMushroomSummaryPage(params)

    if (res.code === 0 && res.data) {
      const pageData = res.data as { items: MushroomSummaryVO[] }
      mushroomList.value = pageData.items.map(item => ({
        id: item.id,
        mushroomName: item.mushroomName
      }))
    } else {
      mushroomList.value = []
    }
  } catch (error) {
    ElMessage.error('加载菌种列表失败')
    console.error(error)
  } finally {
    mushroomLoading.value = false
  }
}

const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')

  if (!isImage) {
    ElMessage.error('只能上传图片格式文件!')
    return false
  }
  
  // 检查文件大小（单个不超过2MB）
  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('单张图片不能超过2MB!')
    return false
  }
  
  return true
}

// 修改：统一处理图片上传，现在只暂存文件而不立即上传
const handleImageUploadSuccess = async (response: any, file: any) => {
  if (!beforeImageUpload(file)) return

  try {
    // 创建临时预览URL
    const localPreviewUrl = URL.createObjectURL(file)
    const generateTempId = () => `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`
    const tempId = generateTempId()
    
    // 添加临时预览图到显示列表
    const tempImageList = [...imageList.value, {
      id: tempId,
      url: localPreviewUrl,
      picUrl: localPreviewUrl,
      isTemporary: true
    }]
    imageList.value = tempImageList
    formData.value.imageList = tempImageList
    activeCarouselIndex.value = tempImageList.length - 1
    await nextTick()
    carouselKey.value = Date.now()

    // 将文件添加到暂存区（不立即上传）
    stagedImageFiles.value.push(file)
    
    ElMessage.success('图片已添加到暂存区')
  } catch (error) {
    console.error('添加图片失败：', error)
    ElMessage.error('添加图片失败')
  }
}

  // 修改 deleteImageById 函数参数类型
  const deleteImageById = async (picId: number | string) => {
    console.log('准备删除的图片ID：', picId)
    
    // 将 picId 转换为数字进行比较（如果是临时ID字符串则跳过API删除）
    const numericId = typeof picId === 'string' ? NaN : picId
    
    // 验证picId有效性
    if (picId === undefined || picId === null || (typeof picId === 'number' && isNaN(picId))) {
      console.warn('无效的图片ID，无法删除')
      return
    }
    
    const index = imageList.value.findIndex(img => img.id === picId)
    if (index === -1) {
      console.warn('未找到对应的图片：', picId)
      return
    }

    try {
      await ElMessageBox.confirm('确定删除这张图片吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      // 如果是临时图片（字符串ID或NaN），直接移除
      const isTempImage = typeof picId === 'string' || isNaN(numericId)
      if (isTempImage) {
        const newImageList = imageList.value.filter(img => img.id !== picId)
        imageList.value = newImageList
        formData.value.imageList = newImageList
        
        // 从暂存区移除对应的文件
        if (typeof picId === 'string') {
          const tempIndex = imageList.value.findIndex((img, i) => img.id === picId)
          if (tempIndex !== -1 && tempIndex < stagedImageFiles.value.length) {
            stagedImageFiles.value.splice(tempIndex, 1)
          }
        }
        
        activeCarouselIndex.value = Math.max(0, index - 1)
        await nextTick()
        carouselKey.value = Date.now()
        ElMessage.success('图片已移除')
        return
      }

      const deleteDTO: PictureDTO = {
        picId: numericId as number
      }
    
      const res = await deletePicture(deleteDTO)
      if (res.code !== 0) {
        ElMessage.error('图片删除失败：' + res.message)
        return
      }

      const newImageList = imageList.value.filter(img => img.id !== picId)
      imageList.value = newImageList
      formData.value.imageList = newImageList
      activeCarouselIndex.value = Math.max(0, index - 1)
      
      await nextTick()
      carouselKey.value = Date.now()

      ElMessage.success('图片删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('图片删除异常：', error)
        ElMessage.error('图片删除失败')
      }
    }
  }

const deleteImage = async (index: number) => {
  // 通过索引从当前imageList中获取图片ID，避免使用可能已失效的引用
  const targetImage = imageList.value[index]
  if (!targetImage) {
    console.warn('未找到要删除的图片，索引:', index)
    return
  }
  
  const picId = targetImage.id
  await deleteImageById(picId)
}

const previewImage = (index: number) => {
  activeCarouselIndex.value = index
}

// 修改：统一处理视频封面上传，现在只暂存文件而不立即上传
const handleVideoCoverUpload = async (response: any, file: any) => {
  try {
    // 创建临时预览URL
    const localPreviewUrl = URL.createObjectURL(file)
    videoCover.value = localPreviewUrl
    formData.value.videoCoverUrl = localPreviewUrl
    
    // 将文件添加到暂存区（不立即上传）
    stagedVideoCoverFile.value = file
    
    ElMessage.success('封面已添加到暂存区')
  } catch (error) {
    ElMessage.error('添加封面失败')
  }
}

// 修改：统一处理视频文件上传，现在只暂存文件而不立即上传
const handleVideoUploadSuccess = async (response: any, file: any) => {
  // 检查视频文件大小（不超过100MB）
  const maxSize = 100 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('视频文件不能超过100MB!')
    return
  }
  
  try {
    // 创建临时预览URL
    const localPreviewUrl = URL.createObjectURL(file)
    formData.value.videoUrl = localPreviewUrl
    videoFileList.value = [{ name: file.name, url: localPreviewUrl }]
    
    // 将文件添加到暂存区（不立即上传）
    stagedVideoFile.value = file
    
    ElMessage.success('视频已添加到暂存区')
  } catch (error) {
    ElMessage.error('添加视频失败')
  }
}

const handleVideoRemove = () => {
  formData.value.videoUrl = ''
  videoFileList.value = []
}

const handleContentEdit = (e: Event) => {
  const target = e.target as HTMLElement
  formData.value.content = target.innerHTML
}

const handleEditorClick = () => {
  // 获取当前选区
  const selection = window.getSelection()
  if (!selection || selection.rangeCount === 0) {
    currentFormat.value = 'p'
    return
  }

  let node = selection.focusNode
  // 如果点击位置在文本节点，向上查找父元素
  if (node && node.nodeType === Node.TEXT_NODE) {
    node = node.parentElement
  }

  // 检查父元素标签类型
  if (node instanceof HTMLElement) {
    const tagName = node.tagName.toLowerCase()
    if (tagName === 'h1' || tagName === 'h2') {
      currentFormat.value = tagName
      return
    }
  }

  // 检查内联样式（加粗、斜体等）
  const range = selection.getRangeAt(0)
  const container = range.commonAncestorContainer
  let parent = container.nodeType === Node.TEXT_NODE ? container.parentElement : container as HTMLElement

  while (parent) {
    if (parent instanceof HTMLElement) {
      if (parent.tagName === 'STRONG' || parent.style.fontWeight === 'bold') {
        currentFormat.value = 'strong'
        return
      }
      if (parent.tagName === 'EM' || parent.style.fontStyle === 'italic') {
        currentFormat.value = 'em'
        return
      }
    }
    parent = parent.parentElement
  }

  // 默认段落格式
  currentFormat.value = 'p'
}

const handleImageError = (e: Event, img?: any) => {
  // 安全检查
  if (!img || !img.id) {
    console.warn('图片加载失败：无效的图片对象', img)
    return
  }

  // 查找当前图片在列表中的最新状态（可能已被删除）
  const index = imageList.value.findIndex(item => item.id === img.id)
  if (index === -1) {
    console.warn(`图片加载失败: 图片ID ${img.id} 已不在列表中`)
    return
  }

  const currentImg = imageList.value[index]
  console.warn(`图片加载失败: ${currentImg.url}`)

  // 记录重试次数
  const retryCount = (imageRetryMap.value[currentImg.id] || 0) + 1
  imageRetryMap.value[currentImg.id] = retryCount

  // 如果重试次数超过最大限制，标记为永久失败（可进一步处理）
  if (retryCount > 3) {
    console.warn(`图片 ${currentImg.id} 重试次数过多，不再自动重试`)
    // 可设置一个标志位，在UI上显示永久失败样式
  }
}

const MAX_RETRY = 3

const retryImageLoad = (img: any) => {
  if (!img || !img.id) return

  const index = imageList.value.findIndex(item => item.id === img.id)
  if (index === -1) return

  const currentImg = imageList.value[index]
  const retryCount = imageRetryMap.value[currentImg.id] || 0

  if (retryCount >= MAX_RETRY) {
    ElMessage.warning('图片重试次数已达上限，请检查图片源')
    return
  }

  // 添加时间戳强制刷新
  const updatedImageList = [...imageList.value]
  updatedImageList[index] = {
    ...currentImg,
    url: currentImg.url.includes('?')
      ? `${currentImg.url}&t=${Date.now()}`
      : `${currentImg.url}?t=${Date.now()}`
  }
  imageList.value = updatedImageList
  formData.value.imageList = updatedImageList

  // 重试次数已经在 handleImageError 中增加，这里不需要再加
}

// 修改 handleSubmit 以支持在提交时上传暂存的文件
const handleSubmit = async () => {
  if (!formData.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (formData.value.mushroomId === null || formData.value.mushroomId === undefined) {  // 修改: 检查null而不是0
    ElMessage.warning('请选择菌种标签')
    return
  }
  if (isArticleType.value && imageList.value.length === 0) {
    ElMessage.warning('请至少上传一张图片')
    return
  }
  if (!isArticleType.value && !formData.value.videoUrl) {
    ElMessage.warning('请上传视频文件')
    return
  }

  // 调试: 打印 authorId
  console.log('当前 authorId:', formData.value.authorId, 'props.authorId:', props.authorId)

  try {
    loading.value = true
    const isEditMode = !!props.editData?.id
    const submitData = new FormData()

    // ========== 1. 构建 addFileDTO 对象（包含所有非文件字段） ==========
    const addFileDTO: any = {
      title: formData.value.title,
      mushroomId: formData.value.mushroomId,
      contentType: formData.value.contentType,
      authorId: Number(formData.value.authorId), // 确保为数字
      description: formData.value.description || undefined,
      // publishDate: formData.value.publishDate || undefined,
    }

    // 图文模式添加正文
    if (isArticleType.value && formData.value.content) {
      addFileDTO.content = formData.value.content
    }

    // 编辑模式下添加 id
    if (isEditMode && formData.value.id) {
      addFileDTO.id = formData.value.id
    }

    // 处理已有的资源 URL（编辑模式）
    if (isEditMode && props.editData) {
      if (isArticleType.value) {
        // 获取所有非临时图片的URL（包括原始URL和可能被替换的URL）
        const existingUrls = imageList.value
          .filter(img => !img.url.startsWith('blob:') && !img.isTemporary)
          .map(img => img.url)
        if (existingUrls.length > 0) {
          addFileDTO.articleDetailPicUrl = existingUrls // 字段名与后端 DTO 一致
        }
      } else {
        // 视频模式：检查是否使用了原始URL（非blob）
        if (formData.value.videoUrl && !formData.value.videoUrl.startsWith('blob:')) {
          addFileDTO.videoUrl = formData.value.videoUrl
        }
        if (formData.value.videoCoverUrl && !formData.value.videoCoverUrl.startsWith('blob:')) {
          addFileDTO.videoCoverUrl = formData.value.videoCoverUrl
        }
      }
    }

    // ========== 2. 将 addFileDTO 作为 JSON 字符串添加到 FormData ==========
    const addFileDTOBlob = new Blob([JSON.stringify(addFileDTO)], { type: 'application/json' });
    submitData.append('addFileDTO', addFileDTOBlob);
    // ========== 3. 添加新上传的文件（字段名与后端 DTO 中 MultipartFile 属性名一致） ==========
    if (isArticleType.value) {
      stagedImageFiles.value.forEach(file => {
        submitData.append('articleDetailFiles', file) // 注意字段名
      })
    } else {
      if (stagedVideoCoverFile.value) {
        submitData.append('videoCoverFile', stagedVideoCoverFile.value)
      }
      if (stagedVideoFile.value) {
        submitData.append('videoFile', stagedVideoFile.value)
      }
    }

    // ========== 4. 发送请求 ==========
    let res
    if (isEditMode) {
      res = await updatePopSciContent(submitData)
    } else {
      console.log('FormData entries:');
      for (let pair of submitData.entries()) {
        console.log(pair[0], pair[1]);
      }
      res = await addPopSciContent(submitData)
    }

    if (res.code === 0) {
      ElMessage.success(isEditMode ? '笔记更新成功' : '笔记创建成功')
      const resData = res.data as { id?: number } | undefined
      if (resData?.id) {
        formData.value.id = resData.id
      }

      emit('saveSuccess', { ...formData.value, id: formData.value.id })
      // 确保在下一个tick中关闭弹窗
      await nextTick()
      handleClose()
    } else {
      ElMessage.error((isEditMode ? '笔记更新' : '笔记创建') + '失败：' + res.message)
    }
  } catch (error) {
    ElMessage.error('操作失败：' + (error instanceof Error ? error.message : '未知错误'))
  } finally {
    loading.value = false
  }
}

// 修改 handleClose 重置逻辑
const handleClose = () => {
  localVisible.value = false
  imageList.value = []
  // 清空所有暂存区数据
  stagedImageFiles.value = []
  videoCover.value = ''
  stagedVideoCoverFile.value = null
  videoCoverId.value = 0
  videoFileList.value = []
  stagedVideoFile.value = null
  formData.value = {
    id: 0,
    title: '',
    mushroomId: null, // 修改: 重置为null而不是0
    mushroomName: '',
    contentType: 'article', // ✅ added
    authorId: Number(props.authorId), // 修改：转换为数字
    content: '',
    description: '',
    imageList: [],
    videoUrl: '',
    videoCoverUrl: ''
  }
  emit('update:modelValue', false)
}

// ========== Watch 放在最后 ==========
watch(() => props.editData, (newVal) => {
  if (newVal) {
    initEditData()
  } else {
    // 新增模式下重置为默认值
    localContentType.value = 'article'
  }
}, { immediate: true, deep: true })

// 监听 localContentType 变化
watch(localContentType, (newVal) => {
  formData.value.contentType = newVal; // 同步 contentType
  
  // 清空相关数据
  if (newVal === 'article') {
    videoCover.value = ''
    videoFileList.value = []
    formData.value.videoUrl = ''
    formData.value.videoCoverUrl = ''
  } else {
    imageList.value = []
    formData.value.imageList = []
  }
})

// 新增：监听 authorId prop 变化，确保 formData 中的 authorId 始终同步
watch(() => props.authorId, (newAuthorId) => {
  if (newAuthorId !== undefined && newAuthorId !== null) {
    formData.value.authorId = typeof newAuthorId === 'string' ? Number(newAuthorId) : newAuthorId;
  }
}, { immediate: true })

// ✅ 修改：确保在弹窗打开时加载菌种列表
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadMushroomList()
  }
})
</script>

<style scoped lang="less">
.pop-sci-edit-dialog {
  .loading-container {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
  }

  .detail-container {
    display: flex;
    width: 100%;
    height: 65vh !important;
    min-height: 600px;
    max-height: 90vh;
    background: #fff;
    border-radius: 12px;
    position: relative;
  }

  .content-box {
    width: 40%;
    height: 100% !important;
    flex-shrink: 0;
    background: #f5f5f5;
    overflow: hidden;
    padding: 16px;
    display: flex;
    flex-direction: column;
  }

  .video-content {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    padding-top: 40px; /* 与图文模式对齐的顶部间距 */
  }

  .video-container {
    width: 100%;
    flex-shrink: 0;
    border-radius: 8px;
    overflow: hidden;
    position: relative;
    background: #f5f5f5;
    height: 68% !important; /* 与图文轮播图高度一致 */

    .video-cover-wrapper {
      width: 100%;
      height: 100%;
      
      .video-cover {
        width: 100% !important;
        height: 100% !important;
        object-fit: cover !important;
        display: block !important;
      }
    }

    .video-upload-btn {
      position: absolute;
      bottom: 16px;
      right: 16px;
    }

    .video-file-upload {
      flex: 1;
      margin-top: 16px; /* 增加与上方容器的间距 */

      .video-upload-area,
      .video-uploaded-area {
        width: 100%;
        height: 100%;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          .text-gray-400,
          .text-gray-500 {
            color: var(--el-color-primary);
          }
        }
      }

      .upload-limit {
        font-size: 12px;
        color: #999;
      }

      .video-uploaded-area {
        border-color: #e6e6e6;
      }
    }
    
    /* 新增：无视频封面时的样式 */
    .no-video-cover-area {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;

      .upload-btn {
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: var(--el-color-primary);
          
          .text-gray-400,
          .text-gray-500 {
            color: var(--el-color-primary);
          }
        }
      }
    }
  }

  .article-content {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    padding-top: 40px; 
    overflow: hidden;
  }

  /* 轮播图容器核心修改：消除多余留白 */
  .carousel-wrapper {
    width: 100%;
    flex: 0 0 68%; /* 固定占父容器68%高度 */
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 0;
  }

  .article-carousel {
    width: 100%;
    height: 100%;
    background: #000;
    border-radius: 8px;

    .carousel-item-wrapper {
      position: relative;
      width: 100%;
      height: 100%;

      .carousel-img {
        width: 100% !important;
        height: 100% !important;
        display: block !important;
        object-fit: cover !important;
      }
    }

    :deep(.el-carousel__container),
    :deep(.el-carousel__item) {
      width: 100% !important;
      height: 100% !important;
      overflow: hidden;
    }

    .img-placeholder,
    .img-error {
      width: 100%;
      height: 100%;
      background: #222;
      color: #fff;
      font-size: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  /* ✅ 图片列表区域：间距缩到最小 */
  .image-list-container {
    width: 100%;
    flex-shrink: 0;
    overflow-x: auto;
    padding: 2px 0;
    margin-top: 4px; /* 关键：和轮播图的间距缩到4px，可自行改为0/2px */

    .image-list {
      display: flex;
      gap: 12px;
      width: max-content;
      align-items: center;
      padding-right: 8px;

      .image-list-item {
        width: 96px;
        height: 96px;
        flex-shrink: 0;
        border-radius: 8px;
        overflow: visible;
        border: 2px solid #e6e6e6;
        transition: all 0.3s;
        position: relative;

        &:hover {
          border-color: var(--el-color-primary);
          transform: translateY(-1px);
        }

        &.border-primary {
          border-color: var(--el-color-primary);
          box-shadow: 0 0 0 2px rgba(var(--el-color-primary-rgb), 0.2);
        }

        .thumbnail-delete-btn {
          transition: all 0.2s;

          &:hover {
            background: #999999;
            transform: scale(1.15);
          }
        }
      }

      /* 列表内的上传按钮样式 */
      .upload-list-item {
        flex-shrink: 0;
        
        .upload-btn-inline {
          cursor: pointer;
          transition: all 0.3s;
          background: #fafafa;

          &:hover {
            border-color: var(--el-color-primary);
            background: #f0f9ff;
            
            .text-gray-400 {
              color: var(--el-color-primary);
            }
          }
        }
      }
    }

    &::-webkit-scrollbar {
      height: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #ccc;
      border-radius: 2px;
      
      &:hover {
        background: #aaa;
      }
    }
  }

  .no-image-area {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    .upload-btn {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: var(--el-color-primary);
        
        .text-gray-400,
        .text-gray-500 {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .edit-box {
    width: 60%;
    flex: 1;
    background: #fff;
    border-left: 1px solid #eee;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .edit-section {
    padding: 24px;
    border-bottom: 1px solid #eee;

    .edit-label {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .title-input {
      margin-top: 12px;
    }
  }

  .edit-section.title-section {
    .content-type-toggle {
      display: flex;
      align-items: center;
      
      :deep(.el-segmented) {
        --el-segmented-item-selected-color: var(--el-text-color-primary);
        --el-segmented-item-selected-bg-color: #f0f9ff;
        --el-border-radius-base: 16px;
        height: 28px;
        
        .el-segmented-item {
          padding: 0 12px;
          font-size: 12px;
        }
      }
    }
  }

  .edit-section.content-section {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
    border-bottom: none;

    .edit-label {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      display: block;
      margin-bottom: 8px;
    }

    .content-editor {
      width: 100%;
      min-height: 200px;
      padding: 16px;
      border: 1px solid #e6e6e6;
      border-radius: 8px;
      outline: none;
      line-height: 1.8;
      font-size: 14px;
      background: #fff;

      h1 {
        font-size: 24px;
        font-weight: bold;
        margin: 16px 0;
        color: #333;
      }

      h2 {
        font-size: 20px;
        font-weight: bold;
        margin: 12px 0;
        color: #333;
      }

      p {
        margin: 8px 0;
        color: #666;
      }

      strong {
        font-weight: bold;
      }

      em {
        font-style: italic;
      }

      &:empty:before {
        content: attr(placeholder);
        color: #999;
        cursor: text;
      }
    }
  }

  .edit-footer {
    padding: 20px 24px;
    border-top: 1px solid #eee;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    flex-shrink: 0;
  }

  .close-btn {
    position: absolute;
    top: 12px;
    right: 12px;
    z-index: 9999 !important;

    :deep(.el-button) {
      width: 40px;
      height: 40px;
      padding: 0;
      background: #fff !important;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1) !important;
      border-radius: 50% !important;

      &:hover {
        background: #f5f5f5 !important;
      }
    }
  }

  @media (max-width: 1200px) {
    .detail-container {
      flex-direction: column;
      height: 90vh !important;
    }

    .content-box {
      width: 100% !important;
      height: 50vh !important;
    }

    .edit-box {
      width: 100% !important;
      height: 50vh !important;
      border-left: none;
      border-top: 1px solid #eee;
    }
  }
}
</style>

<style>
.pop-sci-edit-dialog.el-dialog {
  top: 50% !important;
  transform: translateY(-50%) !important;
  margin: 0 auto !important;
  width: 80% !important;
  border-radius: 16px !important;
  box-shadow: 0 10px 50px rgba(0, 0, 0, 0.2) !important;
  max-height: 90vh !important;
  overflow: hidden !important;
}

.pop-sci-edit-dialog.el-dialog__body {
  height: 90vh !important;
  padding: 0 !important;
  background: #f8f8f8 !important;
}
</style>
