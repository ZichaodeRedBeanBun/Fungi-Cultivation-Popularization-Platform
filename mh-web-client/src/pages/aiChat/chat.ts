import { ref, onMounted } from 'vue'
import type { Ref } from 'vue'
import { ElNotification } from 'element-plus'
import { chatWithMushroomAI, recognizeMushroomImage, Result } from '@/api/system'
import { ChatResponse } from '@/api/interface'

// 修改：扩展ChatResponse接口，直接包含蘑菇识别字段

export interface Message {
  id: string
  content: string
  role: 'user' | 'assistant'
  timestamp: Date
  imageUrl?: string
}

export interface ChatState {
  messages: Ref<Message[]>
  inputText: Ref<string>
  isLoading: Ref<boolean>
  imageFile: Ref<File | null>
  imageUrl: Ref<string | null>
  handleSendMessage: () => Promise<void>
  handleImageUpload: (event: Event) => void
  clearImage: () => void
}

// 新增：辅助函数格式化蘑菇识别结果
const formatMushroomInfo = (info: {
  mushroomName?: string
  isPoison?: number
  toxicity?: string
  confidence?: number
}): string => {
  const { mushroomName, isPoison, toxicity, confidence } = info
  
  // 计算置信度级别
  let confidenceLevel = ''
  if (confidence !== undefined && confidence !== 0) {
    if (confidence >= 0.7) {
      confidenceLevel = '非常有可能'
    } else if (confidence >= 0.4) {
      confidenceLevel = '很有可能'
    } else {
      confidenceLevel = '可能'
    }
  }
  
  // 如果没有识别出具体的蘑菇名称
  if (!mushroomName && isPoison) {
    return `未能识别出具体的蘑菇种类，但根据分析，该菌种${confidenceLevel}有毒，建议谨慎处理，不要食用。`
  }
  
  // 已识别出蘑菇品种
  let poisonText = ''
  // 优先使用 toxicity 字段
  if (toxicity === 'poisonous') {
    poisonText = '该蘑菇有毒！请勿食用！'
  } else if (toxicity === 'conditionally_edible') {
    poisonText = '该蘑菇条件可食（需特殊处理），不建议自行食用。'
  } else if (toxicity === 'edible') {
    poisonText = '该蘑菇无毒，可安全食用。'
  } else if (isPoison === 1) {
    poisonText = '该蘑菇有毒！请勿食用！'
  } else if (isPoison === 0) {
    poisonText = '该蘑菇无毒，可安全食用。'
  } else {
    poisonText = '该蘑菇的安全性无法确定，建议谨慎处理。'
  }
  if (mushroomName===null){
    return `识别结果：暂时不知道时什么菇呢，不过可以确认的是：${poisonText}`
  }
  return `识别结果：${mushroomName}。\n${poisonText}`
}

export const useChat = (): ChatState => {
  const messages = ref<Message[]>([])
  const inputText = ref('')
  const isLoading = ref(false)
  const imageFile = ref<File | null>(null)
  const imageUrl = ref<string | null>(null)

  const handleSendMessage = async () => {
    if (!inputText.value.trim() && !imageFile.value) {
      ElNotification({
        type: 'warning',
        message: '请输入内容或上传图片',
        duration: 2000,
      })
      return
    }

    // === 关键修改：在清空前捕获当前的输入和文件值 ===
    const currentInputText = inputText.value.trim()
    const currentImageFile = imageFile.value
    const currentImageUrl = imageUrl.value

    // Add user message
    const userMessage: Message = {
      id: Date.now().toString(),
      content: currentInputText, // 使用捕获的值
      role: 'user',
      timestamp: new Date(),
      imageUrl: currentImageUrl || undefined,
    }
    messages.value.push(userMessage)

    // Clear input (现在可以安全清空)
    inputText.value = ''
    imageFile.value = null
    imageUrl.value = null

    // Show loading state
    isLoading.value = true

    // 添加"稍等一下，我在查询了~"的临时消息
    const loadingMessageId = (Date.now() + 1).toString();
    const loadingMessage: Message = {
      id: loadingMessageId,
      content: '稍等一下，我在查询了~',
      role: 'assistant',
      timestamp: new Date(),
    }
    messages.value.push(loadingMessage);

    try {
      let responseContent = ''

      // 调用API获取响应
      const formData = new FormData()
      if (currentInputText) {
        formData.append('message', currentInputText)
      }
      if (currentImageFile) {
        formData.append('image', currentImageFile)
      }
      const response = await chatWithMushroomAI(formData) as Result<ChatResponse>
      
      // 处理响应
      if (response.code === 0 && response.data) {
        const chatResponse = response.data
        
        // ========== 根据三种情况处理回复 ==========
        const hasImage = currentImageFile !== null
        const hasText = currentInputText.length > 0
        const hasMushroomInfo = chatResponse.mushroomName !== undefined || chatResponse.isPoison !== undefined
        
        // 情况 1: 只传文字 - 直接显示 reply
        if (!hasImage && hasText) {
          responseContent = chatResponse.reply || '抱歉，我暂时无法回答这个问题。'
        }
        // 情况 2: 只传图片 - 显示识别结果 + reply
        else if (hasImage && !hasText) {
          if (hasMushroomInfo) {
            // 拼接识别结果
            const resultText = formatMushroomInfo({
              mushroomName: chatResponse.mushroomName,
              isPoison: chatResponse.isPoison,
              toxicity: chatResponse.toxicity,
              confidence: chatResponse.confidence
            })
            
            // 修改：根据毒性类型决定是否包含AI回复
            if (chatResponse.toxicity === 'poisonous' || 
                (chatResponse.isPoison === 1 && !chatResponse.toxicity)) {
              // 有毒情况：显示识别结果 + AI说明
              responseContent = resultText + '\n\n' + (chatResponse.reply || '')
            } else {
              // 可食用/条件可食：只显示识别结果
              responseContent = resultText
            }
          } else {
            responseContent = chatResponse.reply || '抱歉，未能识别出蘑菇的有效信息。'
          }
        }
        // 情况 3: 既传图片又传文字
        else if (hasImage && hasText) {
          if (hasMushroomInfo && chatResponse.isPoison === 1 && chatResponse.mushroomName) {
            // 有毒且识别到品种 - 显示科普内容（AI 已根据品种生成）
            responseContent = `识别结果：${chatResponse.mushroomName}\n${chatResponse.reply}`
          } else {
            // 无毒或没有识别到品种 - 根据用户问题回答
            responseContent = chatResponse.reply || '抱歉，我暂时无法回答这个问题。'
          }
        }
      } else {
        responseContent = '抱歉，服务暂时不可用，请稍后再试。'
      }

      // 替换临时消息为实际响应内容
      const updatedMessages = [...messages.value]
      const loadingMsgIndex = updatedMessages.findIndex(msg => msg.id === loadingMessageId)
      if (loadingMsgIndex !== -1) {
        updatedMessages[loadingMsgIndex] = {
          ...updatedMessages[loadingMsgIndex],
          content: responseContent
        }
        messages.value = updatedMessages
      }

      isLoading.value = false
    } catch (error) {
      ElNotification({
        type: 'error',
        message: '请求失败，请稍后重试',
        duration: 3000,
      })
      // 替换临时消息为错误信息
      const updatedMessages = [...messages.value]
      const loadingMsgIndex = updatedMessages.findIndex(msg => msg.id === loadingMessageId)
      if (loadingMsgIndex !== -1) {
        updatedMessages[loadingMsgIndex] = {
          ...updatedMessages[loadingMsgIndex],
          content: '抱歉，请求失败了，请稍后重试。'
        }
        messages.value = updatedMessages
      }
      isLoading.value = false
    }
  }

  const handleImageUpload = (event: Event) => {
    const target = event.target as HTMLInputElement
    if (target.files && target.files[0]) {
      const file = target.files[0]
      imageFile.value = file
      const reader = new FileReader()
      reader.onload = (e) => {
        imageUrl.value = e.target?.result as string
      }
      reader.readAsDataURL(file)
    }
  }

  const clearImage = () => {
    imageFile.value = null
    imageUrl.value = null
  }

  // Initialize with welcome message
  onMounted(() => {
    const welcomeMessage: Message = {
      id: 'welcome',
      content: '您好！我是毒菇识别小助手，您可以输入文字或上传图片与我对话~',
      role: 'assistant',
      timestamp: new Date(),
    }
    messages.value.push(welcomeMessage)
  })

  return {
    messages,
    inputText,
    isLoading,
    imageFile,
    imageUrl,
    handleSendMessage,
    handleImageUpload,
    clearImage,
  }
}