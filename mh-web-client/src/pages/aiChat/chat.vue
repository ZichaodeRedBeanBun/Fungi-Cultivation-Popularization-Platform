<script setup lang="ts">
import { useChat } from './chat'

// Use chat composable
const {
  messages,
  inputText,
  isLoading,
  imageFile,
  imageUrl,
  handleSendMessage,
  handleImageUpload,
  clearImage,
} = useChat()
</script>

<template>
  <div class="flex h-full flex-col">
    <div class="flex-1 overflow-hidden flex flex-col">
      <!-- Chat header -->
      <div class="bg-background border-b p-4">
        <h1 class="text-xl font-bold text-foreground">毒菇识别小助手</h1>
        <p class="text-sm text-muted-foreground mt-1">AI智能对话，支持文字和图片输入</p>
      </div>
      
      <!-- Messages container -->
      <div 
        class="flex-1 overflow-y-auto p-4 space-y-4 bg-muted/30"
        ref="messagesContainer"
      >
        <TransitionGroup name="message-fade">
          <div 
            v-for="message in messages" 
            :key="`${message.id}-${message.content}`"
            class="flex"
            :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
          >
            <div 
              class="max-w-[80%] rounded-2xl px-4 py-2"
              :class="message.role === 'user' 
                ? 'bg-primary text-primary-foreground rounded-br-none' 
                : 'bg-card text-card-foreground rounded-bl-none'"
            >
              <!-- Image preview for user messages -->
              <div v-if="message.imageUrl" class="mb-2">
                <img 
                  :src="message.imageUrl" 
                  alt="Uploaded" 
                  class="max-h-48 rounded-lg object-contain"
                />
              </div>
              
              <div class="whitespace-pre-wrap">{{ message.content }}</div>
              
              <div 
                class="text-xs opacity-70 mt-1"
                :class="message.role === 'user' ? 'text-right' : 'text-left'"
              >
                {{ message.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}
              </div>
            </div>
          </div>
        </TransitionGroup>
        
        <!-- Loading indicator -->
        <div v-if="isLoading" class="flex justify-start">
          <div class="bg-card text-card-foreground rounded-2xl rounded-bl-none px-4 py-2">
            <div class="flex space-x-2">
              <div class="w-2 h-2 rounded-full bg-muted animate-bounce"></div>
              <div class="w-2 h-2 rounded-full bg-muted animate-bounce" style="animation-delay: 0.2s"></div>
              <div class="w-2 h-2 rounded-full bg-muted animate-bounce" style="animation-delay: 0.4s"></div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Input area -->
      <div class="bg-background border-t p-4">
        <!-- Image preview -->
        <div v-if="imageUrl" class="mb-3 relative inline-block">
          <img :src="imageUrl" alt="Preview" class="max-h-24 rounded-lg object-cover" />
          <button 
            @click="clearImage"
            class="absolute -top-2 -right-2 bg-destructive text-destructive-foreground rounded-full w-6 h-6 flex items-center justify-center"
          >
            <icon-tabler:x class="w-4 h-4" />
          </button>
        </div>
        
        <!-- Input with image upload -->
        <div class="flex items-end space-x-2">
          <label 
            for="image-upload" 
            class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 hover:bg-accent hover:text-accent-foreground h-10 w-10"
          >
            <icon-akar-icons:image class="w-5 h-5" />
            <input 
              id="image-upload" 
              type="file" 
              accept="image/*" 
              @change="handleImageUpload"
              class="hidden"
            />
          </label>
          
          <div class="flex-1 relative">
            <textarea
              v-model="inputText"
              @keyup.enter.exact.prevent="handleSendMessage"
              placeholder="输入您的问题..."
              class="flex w-full rounded-lg border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-0 min-h-[40px] max-h-32 resize-none"
              :disabled="isLoading"
            ></textarea>
          </div>
          
          <button
            @click="handleSendMessage"
            :disabled="(!inputText.trim() && !imageFile) || isLoading"
            class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 w-10"
          >
            <icon-tabler:send class="w-5 h-5" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Ensure proper scrolling behavior */
.messages-container {
  scroll-behavior: smooth;
}

/* Message fade transition */
.message-fade-enter-active,
.message-fade-leave-active {
  transition: opacity 0.3s ease;
}
.message-fade-enter-from,
.message-fade-leave-to {
  opacity: 0;
}
</style>